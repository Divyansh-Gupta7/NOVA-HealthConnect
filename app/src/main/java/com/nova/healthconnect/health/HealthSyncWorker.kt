package com.nova.healthconnect.health

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nova.healthconnect.NovaApplication
import com.nova.healthconnect.data.models.SyncAuthException
import com.nova.healthconnect.data.models.SyncValidationException

private const val TAG = "NovaHealthSyncWorker"

/**
 * WorkManager worker that performs a background health sync.
 * Delegates entirely to [HealthSyncManager.syncNow] — no HC logic here.
 */
class HealthSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Background sync starting")
        return try {
            val syncManager = NovaApplication.instance.healthSyncManager
            val result = syncManager.syncNow(syncType = "interval")

            if (result.isSuccess) {
                val data = result.getOrThrow()
                Log.d(TAG, "Background sync complete — ${data.totalRecords()} records")
                Result.success()
            } else {
                val ex = result.exceptionOrNull()
                when (ex) {
                    is SyncValidationException -> {
                        Log.e(TAG, "Background sync permanent validation failure: ${ex.message}. Aborting retry.")
                        Result.failure()
                    }
                    is SyncAuthException -> {
                        Log.e(TAG, "Background sync authentication failure: ${ex.message}. Aborting retry.")
                        Result.failure()
                    }
                    else -> {
                        Log.w(TAG, "Background sync transient failure: ${ex?.message}. Retrying.")
                        Result.retry()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected worker error: ${e.message}", e)
            Result.retry()
        }
    }
}
