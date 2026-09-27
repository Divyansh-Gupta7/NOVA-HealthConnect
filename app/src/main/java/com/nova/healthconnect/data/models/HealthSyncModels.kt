package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class HealthMetricsPayload(
    @SerializedName("steps") val steps: Long = 0,
    @SerializedName("sleepDurationHours") val sleepDurationHours: Double = 0.0,
    @SerializedName("sleepStageDeepHours") val sleepStageDeepHours: Double = 0.0,
    @SerializedName("restingHeartRate") val restingHeartRate: Double = 0.0,
    @SerializedName("avgHeartRate") val avgHeartRate: Double = 0.0,
    @SerializedName("activeCaloriesBurned") val activeCaloriesBurned: Double = 0.0,
    @SerializedName("exerciseSessionMinutes") val exerciseSessionMinutes: Int = 0
)

data class HealthSyncRequest(
    @SerializedName("syncTimestamp") val syncTimestamp: String,
    @SerializedName("date") val date: String,
    @SerializedName("deviceInfo") val deviceInfo: String = "Android Health Connect",
    @SerializedName("metrics") val metrics: HealthMetricsPayload
)

data class HealthSyncResponse(
    val status: String = "success",
    val message: String = "Health data synced successfully",
    val recordsProcessed: Int = 0,
    val syncedAt: String = ""
)
