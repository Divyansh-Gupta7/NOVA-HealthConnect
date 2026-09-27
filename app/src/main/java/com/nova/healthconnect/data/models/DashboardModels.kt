package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class UserSummary(
    val id: String = "user_demo",
    val name: String = "Nova Explorer",
    val email: String = "explorer@nova.internal",
    val status: String = "Ready",
    val streak: Int = 5,
    val activeSession: Boolean = false
)

data class HealthMetrics(
    @SerializedName("sleepHours") val sleepHours: Double = 7.5,
    @SerializedName("sleepQuality") val sleepQuality: Int = 85,
    @SerializedName("restingHeartRate") val restingHeartRate: Int = 62,
    @SerializedName("steps") val steps: Int = 8420,
    @SerializedName("activeMinutes") val activeMinutes: Int = 45,
    @SerializedName("recoveryScore") val recoveryScore: Int = 84,
    @SerializedName("caloriesBurned") val caloriesBurned: Double = 450.0
)

data class CognitiveMetrics(
    @SerializedName("readinessScore") val readinessScore: Int = 85,
    @SerializedName("focusScore") val focusScore: Int = 78,
    @SerializedName("mentalFatigue") val mentalFatigue: String = "Low",
    @SerializedName("predictedPeak") val predictedPeak: String = "10:30 AM"
)

data class FocusSummary(
    @SerializedName("sessionsCompleted") val sessionsCompleted: Int = 3,
    @SerializedName("totalMinutes") val totalMinutes: Int = 75,
    @SerializedName("currentStreak") val currentStreak: Int = 3,
    @SerializedName("targetMinutes") val targetMinutes: Int = 90
)

data class HabitItem(
    val id: String,
    val name: String,
    val completed: Boolean = false,
    val category: String = "general",
    val icon: String = "✦"
)

data class DashboardData(
    val date: String = "",
    val user: UserSummary = UserSummary(),
    val health: HealthMetrics = HealthMetrics(),
    val cognitive: CognitiveMetrics = CognitiveMetrics(),
    val focus: FocusSummary = FocusSummary(),
    val habits: List<HabitItem> = emptyList(),
    val dailyInsight: String = "Optimal recovery window detected. Schedule complex deep work sprint this morning."
)

data class DashboardResponse(
    val status: String = "success",
    val data: DashboardData? = null,
    val message: String? = null
)
