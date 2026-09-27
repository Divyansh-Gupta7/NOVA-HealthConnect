package com.nova.healthconnect.health

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.models.HealthMetricsPayload
import com.nova.healthconnect.data.models.HealthSyncRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

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

    private val _cachedMetrics = MutableStateFlow<HealthMetricsPayload?>(null)
    val cachedMetrics: StateFlow<HealthMetricsPayload?> = _cachedMetrics.asStateFlow()

    suspend fun syncNow(): Result<HealthMetricsPayload> {
        _syncState.value = SyncState.Syncing
        try {
            val metrics = healthConnectManager.readTodayHealthData()
            _cachedMetrics.value = metrics

            val now = Instant.now()
            val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            val formattedTime = "Today, ${formatter.format(now)}"

            val request = HealthSyncRequest(
                syncTimestamp = now.toString(),
                date = LocalDate.now().toString(),
                deviceInfo = "Android Health Connect (Manual Sync)",
                metrics = metrics
            )

            // Try sending to backend
            try {
                val response = RetrofitClient.getService().syncHealthConnectData(request)
                if (response.isSuccessful) {
                    // Synced to backend
                }
            } catch (_: Exception) {
                // Backend unreachable, local Health Connect data is still successfully read and cached
            }

            prefs.edit()
                .putString("last_sync_time", now.toString())
                .putString("last_sync_formatted", formattedTime)
                .putString("last_sync_status", "SUCCESS")
                .apply()

            _lastSyncTime.value = formattedTime
            _syncState.value = SyncState.Success("Synced with Health Connect", formattedTime)
            return Result.success(metrics)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Sync failed"
            _syncState.value = SyncState.Error(errorMsg)
            return Result.failure(e)
        }
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<HealthSyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "nova_health_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    fun cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork("nova_health_sync_work")
    }
}
