package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class CheckInRequest(
    @SerializedName("date") val date: String,
    @SerializedName("sleepQuality") val sleepQuality: Int,
    @SerializedName("stressLevel") val stressLevel: Int,
    @SerializedName("energyLevel") val energyLevel: Int,
    @SerializedName("mood") val mood: String,
    @SerializedName("reactionTimeMs") val reactionTimeMs: Long,
    @SerializedName("habitsCompleted") val habitsCompleted: List<String> = emptyList(),
    @SerializedName("notes") val notes: String = ""
)

data class CheckInResponse(
    val status: String = "success",
    val message: String = "Check-in recorded successfully",
    val cognitiveScore: Int = 85,
    val recommendation: String? = null
)
