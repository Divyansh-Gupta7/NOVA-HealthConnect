package com.nova.healthconnect.health

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.models.BasalBodyTempMetric
import com.nova.healthconnect.data.models.BloodPressureMetric
import com.nova.healthconnect.data.models.ExerciseMetric
import com.nova.healthconnect.data.models.HeartRateMetric
import com.nova.healthconnect.data.models.HealthMetricsPayload
import com.nova.healthconnect.data.models.HealthSyncRequest
import com.nova.healthconnect.data.models.LabeledMetric
import com.nova.healthconnect.data.models.NovaHealthMetrics
import com.nova.healthconnect.data.models.NovaHealthSyncRequest
import com.nova.healthconnect.data.models.NutritionMetric
import com.nova.healthconnect.data.models.SingleValueMetric
import com.nova.healthconnect.data.models.SkinTemperatureMetric
import com.nova.healthconnect.data.models.SleepMetric
import com.nova.healthconnect.data.models.SleepStageMetric
import com.nova.healthconnect.data.models.StepMetric
import com.nova.healthconnect.data.models.SexualActivityMetric
import com.nova.healthconnect.data.models.TimestampMetric
import com.nova.healthconnect.data.models.WindowMetric
import com.nova.healthconnect.data.models.WindowedValueMetric
import com.nova.healthconnect.data.models.SyncValidationException
import com.nova.healthconnect.data.models.SyncAuthException
import com.nova.healthconnect.data.models.SyncServerException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val TAG = "NovaHealthSyncManager"

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String, val syncedAt: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

class HealthSyncManager(
    private val context: Context,
    private val healthConnectManager: HealthConnectManager
) {
    private val prefs = context.getSharedPreferences("nova_sync_prefs", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(
        prefs.getString("last_sync_formatted", "Not yet synced")
    )
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    // Caches the latest NovaHealthData so the UI can display it without re-reading HC
    private val _lastHealthData = MutableStateFlow<NovaHealthData?>(null)
    val lastHealthData: StateFlow<NovaHealthData?> = _lastHealthData.asStateFlow()

    // Legacy: still exposed for screens that depend on HealthMetricsPayload
    private val _cachedMetrics = MutableStateFlow<HealthMetricsPayload?>(null)
    val cachedMetrics: StateFlow<HealthMetricsPayload?> = _cachedMetrics.asStateFlow()

    // ---------------------------------------------------------------------------
    // Incremental sync — tracks the last time each sync ran so we only fetch new data
    // ---------------------------------------------------------------------------
    private fun getLastSyncTime(): Instant? {
        val millis = prefs.getLong("last_sync_epoch_ms", 0L)
        return if (millis > 0) Instant.ofEpochMilli(millis) else null
    }

    private fun saveLastSyncTime(time: Instant) {
        prefs.edit().putLong("last_sync_epoch_ms", time.toEpochMilli()).apply()
    }

    // ---------------------------------------------------------------------------
    // Main sync — reads from HC and posts to NOVA backend
    // ---------------------------------------------------------------------------
    suspend fun syncNow(
        forceFullRead: Boolean = false,
        syncType: String = "manual"
    ): Result<NovaHealthData> {
        _syncState.value = SyncState.Syncing

        return try {
            val now = Instant.now()
            val midnight = now.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()

            val lastSync = if (forceFullRead) null else getLastSyncTime()
            // 5-minute safety overlap to catch late-arriving wearable writes
            val sinceTime = lastSync?.minus(java.time.Duration.ofMinutes(5))
            val windowStart = sinceTime ?: midnight

            // Read from Health Connect using the incremental time boundary
            val healthData = healthConnectManager.readHealthData(
                windowStart = windowStart,
                windowEnd = now,
                sinceTime = sinceTime
            )
            _lastHealthData.value = healthData

            // Build the legacy summary for UI dashboard
            _cachedMetrics.value = healthData.toLegacyPayload()

            // Build and post the rich schema_version 2 payload to NOVA backend
            val richRequest = NovaHealthSyncRequest(
                syncTimestamp = now.toString(),
                windowStart = healthData.readWindowStart.toString(),
                windowEnd = healthData.readWindowEnd.toString(),
                syncType = syncType,
                totalRecords = healthData.totalRecords(),
                metrics = healthData.toNovaHealthMetrics()
            )

            // Transmit to NOVA backend
            val response = RetrofitClient.getService().syncHealthConnectData(richRequest)
            if (!response.isSuccessful) {
                val code = response.code()
                val errBody = response.errorBody()?.string() ?: ""
                val msg = "Backend sync failed with HTTP $code: ${response.message()} - $errBody"
                Log.e(TAG, msg)
                throw when (code) {
                    400 -> SyncValidationException(msg)
                    401 -> SyncAuthException(msg)
                    else -> SyncServerException(msg)
                }
            }

            val respBody = response.body()
            if (respBody?.success != true) {
                val err = respBody?.error?.message ?: "Backend rejected sync payload"
                Log.e(TAG, "Backend sync rejected: $err")
                throw SyncServerException(err)
            }

            // Advance last-sync timestamp ONLY after confirmed backend ingestion
            saveLastSyncTime(now)

            val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            val formattedTime = "Today, ${formatter.format(now)}"

            prefs.edit()
                .putString("last_sync_formatted", formattedTime)
                .putString("last_sync_status", "SUCCESS")
                .apply()

            _lastSyncTime.value = formattedTime
            _syncState.value = SyncState.Success(
                "Synced ${healthData.totalRecords()} records to NOVA",
                formattedTime
            )

            Result.success(healthData)

        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Sync failed"
            Log.e(TAG, "Health sync failed: $msg", e)
            _syncState.value = SyncState.Error(msg)
            prefs.edit().putString("last_sync_status", "FAILED").apply()
            Result.failure(e)
        }
    }

    // ---------------------------------------------------------------------------
    // WorkManager periodic sync — schedules a background sync every [intervalHours] hours
    // ---------------------------------------------------------------------------
    fun schedulePeriodicSync(intervalHours: Long = 1L) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<HealthSyncWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "nova_health_sync_work",
            ExistingPeriodicWorkPolicy.UPDATE, // UPDATE replaces with new interval if changed
            syncRequest
        )
    }

    fun cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork("nova_health_sync_work")
    }
}

// ---------------------------------------------------------------------------
// Mapping: NovaHealthData → NovaHealthMetrics (for the rich JSON payload)
// Maps record_id from NovaRecordOrigin to enable backend deduplication
// ---------------------------------------------------------------------------

private fun NovaHealthData.toNovaHealthMetrics(): NovaHealthMetrics = NovaHealthMetrics(
    steps = steps.map {
        StepMetric(it.count, it.windowStart.toString(), it.windowEnd.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    sleep = sleep.map { session ->
        SleepMetric(
            sessionStart = session.sessionStart.toString(),
            sessionEnd = session.sessionEnd.toString(),
            totalDurationMinutes = session.totalDurationMinutes,
            stages = session.stages.map { stage ->
                SleepStageMetric(stage.stage, stage.startTime.toString(), stage.endTime.toString(), stage.durationMinutes)
            },
            sourceApp = session.origin?.sourceApp,
            recordId = session.origin?.recordId
        )
    },
    heartRate = heartRate.map {
        HeartRateMetric(it.bpm, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    restingHeartRate = restingHeartRate.map {
        SingleValueMetric(it.bpm.toDouble(), it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    heartRateVariability = heartRateVariability.map {
        SingleValueMetric(it.rmssdMillis, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    bloodPressure = bloodPressure.map {
        BloodPressureMetric(it.systolicMmHg, it.diastolicMmHg, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    bloodGlucose = bloodGlucose.map {
        SingleValueMetric(it.mmolPerLiter, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    oxygenSaturation = oxygenSaturation.map {
        SingleValueMetric(it.percentageSpo2, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    bodyTemperature = bodyTemperature.map {
        SingleValueMetric(it.celsius, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    skinTemperature = skinTemperature.map {
        SkinTemperatureMetric(it.deltaCelsius, it.baselineCelsius, it.measurementLocation, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    respiratoryRate = respiratoryRate.map {
        SingleValueMetric(it.breathsPerMinute, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    activeCalories = activeCalories.map {
        WindowedValueMetric(it.kilocalories, it.windowStart.toString(), it.windowEnd.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    totalCalories = totalCalories.map {
        WindowedValueMetric(it.kilocalories, it.windowStart.toString(), it.windowEnd.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    distance = distance.map {
        WindowedValueMetric(it.meters, it.windowStart.toString(), it.windowEnd.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    exercise = exercise.map {
        ExerciseMetric(it.exerciseType, it.title, it.sessionStart.toString(), it.sessionEnd.toString(), it.durationMinutes, it.distanceMeters, it.origin?.sourceApp, it.origin?.recordId)
    },
    weight = weight.map { SingleValueMetric(it.kilograms, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    height = height.map { SingleValueMetric(it.meters, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    bodyFat = bodyFat.map { SingleValueMetric(it.percentage, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    leanBodyMass = leanBodyMass.map { SingleValueMetric(it.kilograms, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    boneMass = boneMass.map { SingleValueMetric(it.kilograms, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    basalMetabolicRate = basalMetabolicRate.map { SingleValueMetric(it.kilocaloriesPerDay, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    vo2Max = vo2Max.map { SingleValueMetric(it.mlPerKgPerMinute, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    nutrition = nutrition.map {
        NutritionMetric(it.mealName, it.windowStart.toString(), it.windowEnd.toString(), it.energyKilocalories, it.proteinGrams, it.carbohydratesGrams, it.fatGrams, it.sugarGrams, it.sodiumGrams, it.fiberGrams, it.origin?.sourceApp, it.origin?.recordId)
    },
    hydration = hydration.map {
        WindowedValueMetric(it.liters, it.windowStart.toString(), it.windowEnd.toString(), it.origin?.sourceApp, it.origin?.recordId)
    },
    menstruationFlow = menstruationFlow.map { LabeledMetric(it.flowLevel, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    menstruationPeriod = menstruationPeriod.map { WindowMetric(it.periodStart.toString(), it.periodEnd.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    intermenstrualBleeding = intermenstrualBleeding.map { TimestampMetric(it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    ovulationTest = ovulationTest.map { LabeledMetric(it.result, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    cervicalMucus = cervicalMucus.map { LabeledMetric(it.appearance, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    sexualActivity = sexualActivity.map { SexualActivityMetric(it.protectionUsed, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) },
    basalBodyTemperature = basalBodyTemperature.map { BasalBodyTempMetric(it.celsius, it.bodyLocation, it.measuredAt.toString(), it.origin?.sourceApp, it.origin?.recordId) }
)

// ---------------------------------------------------------------------------
// Mapping: NovaHealthData → legacy HealthMetricsPayload
// Used for backward compat with the existing NOVA backend endpoint until it
// is upgraded to accept schema_version 2.
// All values are derived from real data — never estimated or fabricated.
// ---------------------------------------------------------------------------

private fun NovaHealthData.toLegacyPayload(): HealthMetricsPayload {
    val totalSteps = steps.sumOf { it.count }

    val totalSleepMinutes = sleep.sumOf { it.totalDurationMinutes }
    val deepSleepMinutes = sleep.flatMap { it.stages }
        .filter { it.stage == "deep" }
        .sumOf { it.durationMinutes }

    // Average BPM across all HR samples; 0.0 if none (never fabricated)
    val avgHr = if (heartRate.isNotEmpty()) {
        heartRate.map { it.bpm }.average()
    } else 0.0

    // Use dedicated resting HR records if available, otherwise 0.0 — no estimation
    val restingHr = if (restingHeartRate.isNotEmpty()) {
        restingHeartRate.minByOrNull { it.measuredAt }?.bpm?.toDouble() ?: 0.0
    } else 0.0

    val totalActiveCal = activeCalories.sumOf { it.kilocalories }

    val totalExerciseMin = exercise.sumOf { it.durationMinutes }.toInt()

    return HealthMetricsPayload(
        steps = totalSteps,
        sleepDurationHours = totalSleepMinutes / 60.0,
        sleepStageDeepHours = deepSleepMinutes / 60.0,
        restingHeartRate = restingHr,
        avgHeartRate = avgHr,
        activeCaloriesBurned = totalActiveCal,
        exerciseSessionMinutes = totalExerciseMin
    )
}
