package com.nova.healthconnect.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.models.HealthSyncRequest
import java.time.Instant
import java.time.LocalDate

class HealthSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val healthConnectManager = HealthConnectManager(applicationContext)

        return try {
            val metrics = healthConnectManager.readTodayHealthData()
            val request = HealthSyncRequest(
                syncTimestamp = Instant.now().toString(),
                date = LocalDate.now().toString(),
                deviceInfo = "Android Health Connect Background Sync",
                metrics = metrics
            )

            val response = RetrofitClient.getService().syncHealthConnectData(request)
            if (response.isSuccessful) {
                val prefs = applicationContext.getSharedPreferences("nova_sync_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("last_sync_time", Instant.now().toString())
                    .putString("last_sync_status", "SUCCESS")
                    .apply()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
