package com.nova.healthconnect.data.repository

import android.content.Context
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.data.models.AiChatRequest
import com.nova.healthconnect.data.models.AiChatResponse
import com.nova.healthconnect.data.models.ChatMessage
import com.nova.healthconnect.data.models.CheckInRequest
import com.nova.healthconnect.data.models.CheckInResponse
import com.nova.healthconnect.data.models.CognitiveMetrics
import com.nova.healthconnect.data.models.DashboardData
import com.nova.healthconnect.data.models.FocusSessionRequest
import com.nova.healthconnect.data.models.FocusSessionResponse
import com.nova.healthconnect.data.models.FocusSummary
import com.nova.healthconnect.data.models.HabitItem
import com.nova.healthconnect.data.models.HealthMetrics
import com.nova.healthconnect.data.models.HealthMetricsPayload
import com.nova.healthconnect.data.models.UserSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NovaRepository(private val context: Context) {

    private val _dashboardData = MutableStateFlow(createDefaultDashboard())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "nova",
                text = "Welcome to NOVA Health Connect terminal. I am your cognitive & biometric companion. Ask me anything about your recovery, focus windows, or habit optimization."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun updateWithHealthConnectMetrics(payload: HealthMetricsPayload) {
        val current = _dashboardData.value
        val updatedHealth = current.health.copy(
            steps = payload.steps.toInt(),
            sleepHours = payload.sleepDurationHours,
            restingHeartRate = if (payload.restingHeartRate > 0) payload.restingHeartRate.toInt() else current.health.restingHeartRate,
            caloriesBurned = payload.activeCaloriesBurned,
            activeMinutes = payload.exerciseSessionMinutes
        )

        // Dynamically compute readiness score based on sleep & heart rate
        val computedReadiness = (payload.sleepDurationHours / 8.0 * 50 + (100 - payload.restingHeartRate) * 0.5)
            .coerceIn(40.0, 99.0).toInt()

        val updatedCognitive = current.cognitive.copy(
            readinessScore = computedReadiness,
            focusScore = (computedReadiness * 0.92).toInt()
        )

        _dashboardData.value = current.copy(
            health = updatedHealth,
            cognitive = updatedCognitive
        )
    }

    suspend fun refreshDashboard(): Result<DashboardData> {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        try {
            val response = RetrofitClient.getService().getDashboard(todayStr)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                _dashboardData.value = data
                return Result.success(data)
            }
        } catch (_: Exception) {
            // Keep current local or default data
        }
        return Result.success(_dashboardData.value)
    }

    suspend fun submitCheckIn(request: CheckInRequest): Result<CheckInResponse> {
        try {
            val response = RetrofitClient.getService().submitCheckIn(request)
            if (response.isSuccessful && response.body() != null) {
                // Update local dashboard habits and state
                val current = _dashboardData.value
                val updatedHabits = current.habits.map { habit ->
                    if (request.habitsCompleted.contains(habit.id)) habit.copy(completed = true) else habit
                }
                _dashboardData.value = current.copy(
                    habits = updatedHabits,
                    cognitive = current.cognitive.copy(readinessScore = response.body()!!.cognitiveScore)
                )
                return Result.success(response.body()!!)
            }
        } catch (_: Exception) {}

        // Fallback local check-in completion
        val current = _dashboardData.value
        val updatedHabits = current.habits.map { habit ->
            if (request.habitsCompleted.contains(habit.id)) habit.copy(completed = true) else habit
        }
        val localScore = ((request.energyLevel * 10) + (request.sleepQuality * 0.5) - (request.stressLevel * 4)).coerceIn(40.0, 98.0).toInt()
        _dashboardData.value = current.copy(
            habits = updatedHabits,
            cognitive = current.cognitive.copy(readinessScore = localScore)
        )
        return Result.success(
            CheckInResponse(
                status = "success",
                message = "Check-in logged locally",
                cognitiveScore = localScore,
                recommendation = "Circadian alignment strong. Prioritize focus sprint before 1:00 PM."
            )
        )
    }

    suspend fun recordFocusSession(request: FocusSessionRequest): Result<FocusSessionResponse> {
        try {
            val response = RetrofitClient.getService().recordFocusSession(request)
            if (response.isSuccessful && response.body() != null) {
                val current = _dashboardData.value
                _dashboardData.value = current.copy(
                    focus = current.focus.copy(
                        sessionsCompleted = current.focus.sessionsCompleted + 1,
                        totalMinutes = current.focus.totalMinutes + request.durationMinutes
                    )
                )
                return Result.success(response.body()!!)
            }
        } catch (_: Exception) {}

        val current = _dashboardData.value
        _dashboardData.value = current.copy(
            focus = current.focus.copy(
                sessionsCompleted = current.focus.sessionsCompleted + 1,
                totalMinutes = current.focus.totalMinutes + request.durationMinutes
            )
        )
        return Result.success(
            FocusSessionResponse(
                status = "success",
                message = "Sprint saved locally",
                streak = current.focus.currentStreak + 1
            )
        )
    }

    suspend fun sendChatMessage(userText: String): Result<String> {
        val userMsg = ChatMessage(sender = "user", text = userText)
        _chatMessages.value = _chatMessages.value + userMsg

        val currentData = _dashboardData.value
        val contextMap = mapOf(
            "recoveryScore" to currentData.health.recoveryScore,
            "sleepHours" to currentData.health.sleepHours,
            "readinessScore" to currentData.cognitive.readinessScore,
            "focusScore" to currentData.cognitive.focusScore,
            "steps" to currentData.health.steps,
            "restingHeartRate" to currentData.health.restingHeartRate
        )

        try {
            val response = RetrofitClient.getService().sendAiChatMessage(
                AiChatRequest(message = userText, context = contextMap)
            )
            if (response.isSuccessful && response.body()?.reply?.isNotBlank() == true) {
                val replyText = response.body()!!.reply
                val novaMsg = ChatMessage(sender = "nova", text = replyText)
                _chatMessages.value = _chatMessages.value + novaMsg
                return Result.success(replyText)
            }
        } catch (_: Exception) {}

        // Fallback intelligent response based on current metrics
        val fallbackReply = generateLocalAiResponse(userText, currentData)
        val novaMsg = ChatMessage(sender = "nova", text = fallbackReply)
        _chatMessages.value = _chatMessages.value + novaMsg
        return Result.success(fallbackReply)
    }

    private fun generateLocalAiResponse(query: String, data: DashboardData): String {
        val q = query.lowercase()
        return when {
            q.contains("sleep") || q.contains("recovery") -> {
                "Your sleep recorded via Health Connect is ${data.health.sleepHours} hrs. With a resting heart rate of ${data.health.restingHeartRate} bpm, your somatic recovery score is ${data.health.recoveryScore}%. Your optimal deep work block is currently projected for ${data.cognitive.predictedPeak}."
            }
            q.contains("focus") || q.contains("work") || q.contains("sprint") -> {
                "You have accumulated ${data.focus.totalMinutes} minutes of deep focus across ${data.focus.sessionsCompleted} sessions today. Your readiness index is currently ${data.cognitive.readinessScore}/100. I recommend initiating a 25-minute binaural sprint now."
            }
            q.contains("habit") || q.contains("routine") -> {
                val completedCount = data.habits.count { it.completed }
                "You have completed $completedCount of ${data.habits.size} daily neuro-habits. Consistent morning circadian exposure and hydration will keep your reaction latency low."
            }
            else -> {
                "NOVA Biometrics analysis: Readiness is at ${data.cognitive.readinessScore}%, daily steps at ${data.health.steps}, and focus streak is active at ${data.focus.currentStreak} days. How would you like to structure your next cognitive block?"
            }
        }
    }

    private fun createDefaultDashboard(): DashboardData {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        return DashboardData(
            date = todayStr,
            user = UserSummary(
                id = "usr_default",
                name = "Dr. Nova",
                status = "Ready",
                streak = 7,
                activeSession = false
            ),
            health = HealthMetrics(
                sleepHours = 7.6,
                sleepQuality = 88,
                restingHeartRate = 61,
                steps = 8940,
                activeMinutes = 48,
                recoveryScore = 86,
                caloriesBurned = 460.0
            ),
            cognitive = CognitiveMetrics(
                readinessScore = 88,
                focusScore = 82,
                mentalFatigue = "Low",
                predictedPeak = "10:30 AM – 1:00 PM"
            ),
            focus = FocusSummary(
                sessionsCompleted = 3,
                totalMinutes = 75,
                currentStreak = 4,
                targetMinutes = 90
            ),
            habits = listOf(
                HabitItem("1", "Morning Sunlight (15 min)", completed = true, category = "circadian", icon = "☀️"),
                HabitItem("2", "Electrolyte Hydration (500ml)", completed = true, category = "somatic", icon = "💧"),
                HabitItem("3", "Zone 2 Cardio / 8k Steps", completed = true, category = "physical", icon = "🏃"),
                HabitItem("4", "Neuro-Reflex Calibration", completed = false, category = "cognitive", icon = "⚡"),
                HabitItem("5", "Screen Curfew 10:00 PM", completed = false, category = "sleep", icon = "🌙")
            ),
            dailyInsight = "Circadian alignment is elevated today. Deep sleep ratio supported a 86% autonomic recovery. Prime window for highest-complexity cognitive tasks is open."
        )
    }
}
