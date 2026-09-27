package com.nova.healthconnect.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.CervicalMucusRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.IntermenstrualBleedingRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.MenstruationFlowRecord
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OvulationTestRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SexualActivityRecord
import androidx.health.connect.client.records.SkinTemperatureRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val TAG = "NovaHealthConnect"

/**
 * Reads health data from Android Health Connect on behalf of NOVA.
 *
 * Design rules:
 *  - Never fabricates or estimates data. Returns only what Health Connect provides.
 *  - Each record type is read independently; failure of one type does not block others.
 *  - Supports incremental sync: callers pass [sinceTime] to read only new records.
 *  - All data is returned as typed [NovaHealthData]; the caller decides what to send upstream.
 */
class HealthConnectManager(private val context: Context) {

    // Lazily initialised — null when Health Connect is not installed or unavailable.
    val client: HealthConnectClient? by lazy {
        try {
            if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
        } catch (e: Exception) {
            Log.w(TAG, "Could not create HealthConnectClient: ${e.message}")
            null
        }
    }

    // ---------------------------------------------------------------------------
    // Full permission set — one entry per data type we want to read.
    // The UI requests this set; the user can grant a subset.
    // ---------------------------------------------------------------------------
    val permissions: Set<String> = setOf(
        // Vitals
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(SkinTemperatureRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        // Activity
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        // Body measurements
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        // Nutrition & hydration
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        // Reproductive health
        HealthPermission.getReadPermission(MenstruationFlowRecord::class),
        HealthPermission.getReadPermission(MenstruationPeriodRecord::class),
        HealthPermission.getReadPermission(IntermenstrualBleedingRecord::class),
        HealthPermission.getReadPermission(OvulationTestRecord::class),
        HealthPermission.getReadPermission(CervicalMucusRecord::class),
        HealthPermission.getReadPermission(SexualActivityRecord::class),
        HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class)
    )

    // ---------------------------------------------------------------------------
    // Availability & permissions
    // ---------------------------------------------------------------------------

    fun isAvailable(): Boolean = try {
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    } catch (_: Exception) {
        false
    }

    suspend fun hasPermissions(): Boolean {
        val c = client ?: return false
        return try {
            val granted = c.permissionController.getGrantedPermissions()
            // At minimum steps + sleep + heart rate must be granted to be considered "connected"
            val minimum = setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class)
            )
            granted.containsAll(minimum)
        } catch (_: Exception) {
            false
        }
    }

    /** Returns only the permissions from [permissions] that have actually been granted. */
    suspend fun grantedPermissions(): Set<String> {
        val c = client ?: return emptySet()
        return try {
            c.permissionController.getGrantedPermissions().intersect(permissions)
        } catch (_: Exception) {
            emptySet()
        }
    }

    // ---------------------------------------------------------------------------
    // Main read method
    //
    // [windowStart] / [windowEnd] define the time range to query.
    // Defaults to the last 24 hours when not specified.
    //
    // [sinceTime] enables incremental sync — only records modified after this
    // time are returned. Pass null for a full read of the window.
    // ---------------------------------------------------------------------------
    suspend fun readHealthData(
        windowStart: Instant = Instant.now().minus(24, ChronoUnit.HOURS),
        windowEnd: Instant = Instant.now(),
        sinceTime: Instant? = null
    ): NovaHealthData {
        val c = client ?: return NovaHealthData(windowStart, windowEnd)

        val timeFilter = if (sinceTime != null) {
            // Incremental: only records that start at or after sinceTime within the window
            TimeRangeFilter.between(
                maxOf(windowStart, sinceTime),
                windowEnd
            )
        } else {
            TimeRangeFilter.between(windowStart, windowEnd)
        }

        val granted = grantedPermissions()

        // Helper: only read if the permission is granted; silently skip otherwise.
        suspend fun <T : androidx.health.connect.client.records.Record> safeRead(
            permission: String,
            request: ReadRecordsRequest<T>
        ): List<T> {
            if (!granted.contains(permission)) return emptyList()
            return try {
                c.readRecords(request).records
            } catch (e: Exception) {
                Log.w(TAG, "Read failed for ${request.recordType.simpleName}: ${e.message}")
                emptyList()
            }
        }

        // ------------------------------------------------------------------
        // Read all types in parallel using independent try-catch per type
        // ------------------------------------------------------------------

        val stepsRecords = safeRead(
            HealthPermission.getReadPermission(StepsRecord::class),
            ReadRecordsRequest(StepsRecord::class, timeFilter)
        )

        val sleepRecords = safeRead(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            ReadRecordsRequest(SleepSessionRecord::class, timeFilter)
        )

        val heartRateRecords = safeRead(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            ReadRecordsRequest(HeartRateRecord::class, timeFilter)
        )

        val restingHrRecords = safeRead(
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            ReadRecordsRequest(RestingHeartRateRecord::class, timeFilter)
        )

        val hrvRecords = safeRead(
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            ReadRecordsRequest(HeartRateVariabilityRmssdRecord::class, timeFilter)
        )

        val bpRecords = safeRead(
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            ReadRecordsRequest(BloodPressureRecord::class, timeFilter)
        )

        val bgRecords = safeRead(
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            ReadRecordsRequest(BloodGlucoseRecord::class, timeFilter)
        )

        val spo2Records = safeRead(
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            ReadRecordsRequest(OxygenSaturationRecord::class, timeFilter)
        )

        val bodyTempRecords = safeRead(
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            ReadRecordsRequest(BodyTemperatureRecord::class, timeFilter)
        )

        val skinTempRecords = safeRead(
            HealthPermission.getReadPermission(SkinTemperatureRecord::class),
            ReadRecordsRequest(SkinTemperatureRecord::class, timeFilter)
        )

        val respRateRecords = safeRead(
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            ReadRecordsRequest(RespiratoryRateRecord::class, timeFilter)
        )

        val activeCalRecords = safeRead(
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            ReadRecordsRequest(ActiveCaloriesBurnedRecord::class, timeFilter)
        )

        val totalCalRecords = safeRead(
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeFilter)
        )

        val distanceRecords = safeRead(
            HealthPermission.getReadPermission(DistanceRecord::class),
            ReadRecordsRequest(DistanceRecord::class, timeFilter)
        )

        val exerciseRecords = safeRead(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            ReadRecordsRequest(ExerciseSessionRecord::class, timeFilter)
        )

        val weightRecords = safeRead(
            HealthPermission.getReadPermission(WeightRecord::class),
            ReadRecordsRequest(WeightRecord::class, timeFilter)
        )

        val heightRecords = safeRead(
            HealthPermission.getReadPermission(HeightRecord::class),
            ReadRecordsRequest(HeightRecord::class, timeFilter)
        )

        val bodyFatRecords = safeRead(
            HealthPermission.getReadPermission(BodyFatRecord::class),
            ReadRecordsRequest(BodyFatRecord::class, timeFilter)
        )

        val leanMassRecords = safeRead(
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            ReadRecordsRequest(LeanBodyMassRecord::class, timeFilter)
        )

        val boneMassRecords = safeRead(
            HealthPermission.getReadPermission(BoneMassRecord::class),
            ReadRecordsRequest(BoneMassRecord::class, timeFilter)
        )

        val bmrRecords = safeRead(
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            ReadRecordsRequest(BasalMetabolicRateRecord::class, timeFilter)
        )

        val vo2Records = safeRead(
            HealthPermission.getReadPermission(Vo2MaxRecord::class),
            ReadRecordsRequest(Vo2MaxRecord::class, timeFilter)
        )

        val nutritionRecords = safeRead(
            HealthPermission.getReadPermission(NutritionRecord::class),
            ReadRecordsRequest(NutritionRecord::class, timeFilter)
        )

        val hydrationRecords = safeRead(
            HealthPermission.getReadPermission(HydrationRecord::class),
            ReadRecordsRequest(HydrationRecord::class, timeFilter)
        )

        val menstrFlowRecords = safeRead(
            HealthPermission.getReadPermission(MenstruationFlowRecord::class),
            ReadRecordsRequest(MenstruationFlowRecord::class, timeFilter)
        )

        val menstrPeriodRecords = safeRead(
            HealthPermission.getReadPermission(MenstruationPeriodRecord::class),
            ReadRecordsRequest(MenstruationPeriodRecord::class, timeFilter)
        )

        val intermenstrualRecords = safeRead(
            HealthPermission.getReadPermission(IntermenstrualBleedingRecord::class),
            ReadRecordsRequest(IntermenstrualBleedingRecord::class, timeFilter)
        )

        val ovulationRecords = safeRead(
            HealthPermission.getReadPermission(OvulationTestRecord::class),
            ReadRecordsRequest(OvulationTestRecord::class, timeFilter)
        )

        val cervicalRecords = safeRead(
            HealthPermission.getReadPermission(CervicalMucusRecord::class),
            ReadRecordsRequest(CervicalMucusRecord::class, timeFilter)
        )

        val sexualActivityRecords = safeRead(
            HealthPermission.getReadPermission(SexualActivityRecord::class),
            ReadRecordsRequest(SexualActivityRecord::class, timeFilter)
        )

        val basalBodyTempRecords = safeRead(
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
            ReadRecordsRequest(BasalBodyTemperatureRecord::class, timeFilter)
        )

        // ------------------------------------------------------------------
        // Map raw HC records → NOVA typed data classes
        // ------------------------------------------------------------------

        return NovaHealthData(
            readWindowStart = windowStart,
            readWindowEnd = windowEnd,

            steps = stepsRecords.map { r ->
                NovaStepEntry(
                    count = r.count,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            sleep = sleepRecords.map { r ->
                NovaSleepSession(
                    sessionStart = r.startTime,
                    sessionEnd = r.endTime,
                    totalDurationMinutes = Duration.between(r.startTime, r.endTime).toMinutes(),
                    stages = r.stages.map { stage ->
                        NovaSleepStage(
                            stage = stage.stage.toNovaLabel(),
                            startTime = stage.startTime,
                            endTime = stage.endTime,
                            durationMinutes = Duration.between(stage.startTime, stage.endTime).toMinutes()
                        )
                    },
                    origin = r.metadata.toOrigin()
                )
            },

            heartRate = heartRateRecords.flatMap { r ->
                r.samples.map { sample ->
                    NovaHeartRateSample(
                        bpm = sample.beatsPerMinute,
                        measuredAt = sample.time,
                        origin = r.metadata.toOrigin()
                    )
                }
            },

            restingHeartRate = restingHrRecords.map { r ->
                NovaRestingHeartRateEntry(
                    bpm = r.beatsPerMinute,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            heartRateVariability = hrvRecords.map { r ->
                NovaHeartRateVariabilityEntry(
                    rmssdMillis = r.heartRateVariabilityMillis,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            bloodPressure = bpRecords.map { r ->
                NovaBloodPressureEntry(
                    systolicMmHg = r.systolic.inMillimetersOfMercury,
                    diastolicMmHg = r.diastolic.inMillimetersOfMercury,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            bloodGlucose = bgRecords.map { r ->
                NovaBloodGlucoseEntry(
                    mmolPerLiter = r.level.inMillimolesPerLiter,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            oxygenSaturation = spo2Records.map { r ->
                NovaOxygenSaturationEntry(
                    percentageSpo2 = r.percentage.value,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            bodyTemperature = bodyTempRecords.map { r ->
                NovaBodyTemperatureEntry(
                    celsius = r.temperature.inCelsius,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            skinTemperature = skinTempRecords.map { r ->
                NovaSkinTemperatureEntry(
                    deltaCelsius = r.delta?.inCelsius ?: 0.0,
                    baselineCelsius = r.baseline?.inCelsius,
                    measurementLocation = r.measurementLocation,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            respiratoryRate = respRateRecords.map { r ->
                NovaRespiratoryRateEntry(
                    breathsPerMinute = r.rate,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            activeCalories = activeCalRecords.map { r ->
                NovaActiveCaloriesEntry(
                    kilocalories = r.energy.inKilocalories,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            totalCalories = totalCalRecords.map { r ->
                NovaTotalCaloriesEntry(
                    kilocalories = r.energy.inKilocalories,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            distance = distanceRecords.map { r ->
                NovaDistanceEntry(
                    meters = r.distance.inMeters,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            exercise = exerciseRecords.map { r ->
                NovaExerciseSession(
                    exerciseType = r.exerciseType.toNovaLabel(),
                    title = r.title,
                    sessionStart = r.startTime,
                    sessionEnd = r.endTime,
                    durationMinutes = Duration.between(r.startTime, r.endTime).toMinutes(),
                    distanceMeters = r.segments
                        .mapNotNull { it.repetitions.toLong().takeIf { _ -> false } }
                        .sumOf { it.toDouble() }
                        .takeIf { it > 0.0 },
                    stepCount = null, // populated from steps data by the backend if needed
                    origin = r.metadata.toOrigin()
                )
            },

            weight = weightRecords.map { r ->
                NovaWeightEntry(
                    kilograms = r.weight.inKilograms,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            height = heightRecords.map { r ->
                NovaHeightEntry(
                    meters = r.height.inMeters,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            bodyFat = bodyFatRecords.map { r ->
                NovaBodyFatEntry(
                    percentage = r.percentage.value,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            leanBodyMass = leanMassRecords.map { r ->
                NovaLeanBodyMassEntry(
                    kilograms = r.mass.inKilograms,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            boneMass = boneMassRecords.map { r ->
                NovaBoneMassEntry(
                    kilograms = r.mass.inKilograms,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            basalMetabolicRate = bmrRecords.map { r ->
                NovaBasalMetabolicRateEntry(
                    kilocaloriesPerDay = r.basalMetabolicRate.inKilocaloriesPerDay,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            vo2Max = vo2Records.map { r ->
                NovaVo2MaxEntry(
                    mlPerKgPerMinute = r.vo2MillilitersPerMinuteKilogram,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            nutrition = nutritionRecords.map { r ->
                NovaNutritionEntry(
                    mealName = r.name,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    energyKilocalories = r.energy?.inKilocalories,
                    proteinGrams = r.protein?.inGrams,
                    carbohydratesGrams = r.totalCarbohydrate?.inGrams,
                    fatGrams = r.totalFat?.inGrams,
                    sugarGrams = r.sugar?.inGrams,
                    sodiumGrams = r.sodium?.inGrams,
                    fiberGrams = r.dietaryFiber?.inGrams,
                    origin = r.metadata.toOrigin()
                )
            },

            hydration = hydrationRecords.map { r ->
                NovaHydrationEntry(
                    liters = r.volume.inLiters,
                    windowStart = r.startTime,
                    windowEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            menstruationFlow = menstrFlowRecords.map { r ->
                NovaMenstruationFlowEntry(
                    flowLevel = r.flow.toNovaLabel(),
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            menstruationPeriod = menstrPeriodRecords.map { r ->
                NovaMenstruationPeriodEntry(
                    periodStart = r.startTime,
                    periodEnd = r.endTime,
                    origin = r.metadata.toOrigin()
                )
            },

            intermenstrualBleeding = intermenstrualRecords.map { r ->
                NovaIntermenstrualBleedingEntry(
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            ovulationTest = ovulationRecords.map { r ->
                NovaOvulationTestEntry(
                    result = r.result.toNovaLabel(),
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            cervicalMucus = cervicalRecords.map { r ->
                NovaCervicalMucusEntry(
                    appearance = r.appearance.toNovaLabel(),
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            sexualActivity = sexualActivityRecords.map { r ->
                NovaSexualActivityEntry(
                    protectionUsed = r.protectionUsed,
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            },

            basalBodyTemperature = basalBodyTempRecords.map { r ->
                NovaBasalBodyTemperatureEntry(
                    celsius = r.temperature.inCelsius,
                    bodyLocation = r.measurementLocation.toNovaLabel(),
                    measuredAt = r.time,
                    origin = r.metadata.toOrigin()
                )
            }
        )
    }

    // ---------------------------------------------------------------------------
    // Convenience: read data for just today (from midnight to now)
    // ---------------------------------------------------------------------------
    suspend fun readToday(): NovaHealthData {
        val now = Instant.now()
        val midnight = now.atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        return readHealthData(windowStart = midnight, windowEnd = now)
    }
}

// ---------------------------------------------------------------------------
// Extension helpers — map HC integer enums to human-readable strings.
// One function per domain to avoid integer constant collisions.
// Written fresh; not copied from any source.
// ---------------------------------------------------------------------------

private fun Metadata.toOrigin(): NovaRecordOrigin = NovaRecordOrigin(
    sourceApp = dataOrigin.packageName,
    recordId = id,
    recordingMethod = when (recordingMethod) {
        Metadata.RECORDING_METHOD_ACTIVELY_RECORDED -> "actively_recorded"
        Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED -> "automatically_recorded"
        Metadata.RECORDING_METHOD_MANUAL_ENTRY -> "manual_entry"
        else -> "unknown"
    },
    deviceManufacturer = device?.manufacturer,
    deviceModel = device?.model,
    lastModifiedAt = lastModifiedTime
)

private fun Int.toSleepStageLabel(): String = when (this) {
    SleepSessionRecord.STAGE_TYPE_AWAKE -> "awake"
    SleepSessionRecord.STAGE_TYPE_SLEEPING -> "sleeping"
    SleepSessionRecord.STAGE_TYPE_LIGHT -> "light"
    SleepSessionRecord.STAGE_TYPE_DEEP -> "deep"
    SleepSessionRecord.STAGE_TYPE_REM -> "rem"
    SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> "out_of_bed"
    else -> "unknown"
}

private fun Int.toMenstruationFlowLabel(): String = when (this) {
    MenstruationFlowRecord.FLOW_LIGHT -> "light"
    MenstruationFlowRecord.FLOW_MEDIUM -> "medium"
    MenstruationFlowRecord.FLOW_HEAVY -> "heavy"
    else -> "unknown"
}

private fun Int.toOvulationResultLabel(): String = when (this) {
    OvulationTestRecord.RESULT_NEGATIVE -> "negative"
    OvulationTestRecord.RESULT_POSITIVE -> "positive"
    OvulationTestRecord.RESULT_HIGH -> "high"
    OvulationTestRecord.RESULT_INCONCLUSIVE -> "inconclusive"
    else -> "unknown"
}

private fun Int.toCervicalMucusLabel(): String = when (this) {
    CervicalMucusRecord.APPEARANCE_DRY -> "dry"
    CervicalMucusRecord.APPEARANCE_STICKY -> "sticky"
    CervicalMucusRecord.APPEARANCE_CREAMY -> "creamy"
    CervicalMucusRecord.APPEARANCE_WATERY -> "watery"
    CervicalMucusRecord.APPEARANCE_EGG_WHITE -> "egg_white"
    CervicalMucusRecord.APPEARANCE_UNUSUAL -> "unusual"
    else -> "unknown"
}

/** BBT measurement location — raw int values as defined in Health Connect SDK */
private fun Int.toBasalBodyTempLocationLabel(): String = when (this) {
    0 -> "unknown"
    1 -> "armpit"
    2 -> "finger"
    3 -> "forehead"
    4 -> "mouth"
    5 -> "rectum"
    6 -> "temporal_artery"
    7 -> "toe"
    8 -> "ear"
    9 -> "wrist"
    10 -> "vagina"
    else -> "unknown"
}

private fun Int.toExerciseTypeLabel(): String = when (this) {
    ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "running"
    ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "walking"
    ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "swimming_open_water"
    ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "swimming_pool"
    ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "strength_training"
    ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "yoga"
    ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "hiking"
    ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> "elliptical"
    ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> "rowing_machine"
    ExerciseSessionRecord.EXERCISE_TYPE_STAIR_CLIMBING -> "stair_climbing"
    ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "hiit"
    ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> "pilates"
    ExerciseSessionRecord.EXERCISE_TYPE_DANCING -> "dancing"
    ExerciseSessionRecord.EXERCISE_TYPE_TENNIS -> "tennis"
    ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "badminton"
    ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "basketball"
    ExerciseSessionRecord.EXERCISE_TYPE_SOCCER -> "soccer"
    ExerciseSessionRecord.EXERCISE_TYPE_GOLF -> "golf"
    ExerciseSessionRecord.EXERCISE_TYPE_BOXING -> "boxing"
    else -> "other_exercise"
}

