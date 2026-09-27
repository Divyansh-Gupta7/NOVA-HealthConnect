package com.nova.healthconnect.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.healthconnect.NovaApplication
import com.nova.healthconnect.data.models.CheckInRequest
import com.nova.healthconnect.data.models.DashboardData
import com.nova.healthconnect.data.models.FocusSessionRequest
import com.nova.healthconnect.health.SyncState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ==========================================
// 1. OVERVIEW VIEW MODEL
// ==========================================
class OverviewViewModel : ViewModel() {
    private val repository = NovaApplication.instance.novaRepository
    private val syncManager = NovaApplication.instance.healthSyncManager

    val dashboardData: StateFlow<DashboardData> = repository.dashboardData
    val syncState: StateFlow<SyncState> = syncManager.syncState
    val lastSyncTime: StateFlow<String?> = syncManager.lastSyncTime

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshDashboard()
            _isRefreshing.value = false
        }
    }

    fun syncHealthConnect() {
        viewModelScope.launch {
            val result = syncManager.syncNow()
            if (result.isSuccess) {
                // cachedMetrics is updated by syncNow() — use it to update the repository's dashboard
                syncManager.cachedMetrics.value?.let { payload ->
                    repository.updateWithHealthConnectMetrics(payload)
                }
            }
        }
    }

    fun toggleHabit(habitId: String) {
        val current = dashboardData.value
        val updatedHabits = current.habits.map {
            if (it.id == habitId) it.copy(completed = !it.completed) else it
        }
        val habitsCompleted = updatedHabits.filter { it.completed }.map { it.id }
        viewModelScope.launch {
            repository.submitCheckIn(
                CheckInRequest(
                    date = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    sleepQuality = current.health.sleepQuality,
                    stressLevel = 2,
                    energyLevel = 4,
                    mood = "Calibrated",
                    reactionTimeMs = 280,
                    habitsCompleted = habitsCompleted
                )
            )
        }
    }
}

// ==========================================
// 2. HEALTH VIEW MODEL
// ==========================================
class HealthViewModel : ViewModel() {
    private val healthConnectManager = NovaApplication.instance.healthConnectManager
    private val syncManager = NovaApplication.instance.healthSyncManager
    private val repository = NovaApplication.instance.novaRepository

    val isAvailable: Boolean = healthConnectManager.isAvailable()
    val permissions = healthConnectManager.permissions

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val lastSyncTime: StateFlow<String?> = syncManager.lastSyncTime
    val cachedMetrics = syncManager.cachedMetrics
    val dashboardData: StateFlow<DashboardData> = repository.dashboardData

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _hasPermissions.value = healthConnectManager.hasPermissions()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = syncManager.syncNow()
            if (result.isSuccess) {
                syncManager.cachedMetrics.value?.let { payload ->
                    repository.updateWithHealthConnectMetrics(payload)
                }
            }
        }
    }
}

// ==========================================
// 3. FOCUS VIEW MODEL
// ==========================================
enum class FocusTimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

class FocusViewModel : ViewModel() {
    private val repository = NovaApplication.instance.novaRepository

    private val _timerState = MutableStateFlow(FocusTimerState.IDLE)
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    private val _durationMinutes = MutableStateFlow(25)
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(25 * 60)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _distractionsCount = MutableStateFlow(0)
    val distractionsCount: StateFlow<Int> = _distractionsCount.asStateFlow()

    private val _selectedSoundscape = MutableStateFlow("Binaural 40Hz")
    val selectedSoundscape: StateFlow<String> = _selectedSoundscape.asStateFlow()

    private var timerJob: Job? = null

    fun setDuration(minutes: Int) {
        if (_timerState.value == FocusTimerState.IDLE) {
            _durationMinutes.value = minutes
            _secondsRemaining.value = minutes * 60
        }
    }

    fun setSoundscape(sound: String) {
        _selectedSoundscape.value = sound
    }

    fun startTimer() {
        if (_timerState.value == FocusTimerState.IDLE || _timerState.value == FocusTimerState.PAUSED) {
            _timerState.value = FocusTimerState.RUNNING
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_secondsRemaining.value > 0 && _timerState.value == FocusTimerState.RUNNING) {
                    delay(1000)
                    _secondsRemaining.value -= 1
                }
                if (_secondsRemaining.value <= 0) {
                    completeSession()
                }
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == FocusTimerState.RUNNING) {
            _timerState.value = FocusTimerState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = FocusTimerState.IDLE
        _secondsRemaining.value = _durationMinutes.value * 60
        _distractionsCount.value = 0
    }

    fun logDistraction() {
        if (_timerState.value == FocusTimerState.RUNNING) {
            _distractionsCount.value += 1
        }
    }

    private fun completeSession() {
        _timerState.value = FocusTimerState.COMPLETED
        viewModelScope.launch {
            repository.recordFocusSession(
                FocusSessionRequest(
                    type = "focus",
                    durationMinutes = _durationMinutes.value,
                    distractionsLogged = _distractionsCount.value,
                    completed = true,
                    soundscape = _selectedSoundscape.value
                )
            )
        }
    }
}

// ==========================================
// 4. CHECK-IN & REACTION TEST VIEW MODEL
// ==========================================
enum class ReactionState {
    READY, WAITING, CLICK_NOW, FINISHED, TOO_EARLY
}

class CheckInViewModel : ViewModel() {
    private val repository = NovaApplication.instance.novaRepository

    val dashboardData = repository.dashboardData

    var sleepQuality = MutableStateFlow(85)
    var stressLevel = MutableStateFlow(2)
    var energyLevel = MutableStateFlow(4)
    var mood = MutableStateFlow("Focused")
    val selectedHabitIds = MutableStateFlow<Set<String>>(emptySet())

    // Neuro-Reflex Reaction Test
    private val _reactionState = MutableStateFlow(ReactionState.READY)
    val reactionState: StateFlow<ReactionState> = _reactionState.asStateFlow()

    private val _reactionTimeMs = MutableStateFlow(274L)
    val reactionTimeMs: StateFlow<Long> = _reactionTimeMs.asStateFlow()

    private var waitJob: Job? = null
    private var startTimeMs: Long = 0

    init {
        // Preload habits
        viewModelScope.launch {
            dashboardData.collect { data ->
                val completed = data.habits.filter { it.completed }.map { it.id }.toSet()
                selectedHabitIds.value = completed
            }
        }
    }

    fun toggleHabit(habitId: String) {
        val current = selectedHabitIds.value.toMutableSet()
        if (current.contains(habitId)) {
            current.remove(habitId)
        } else {
            current.add(habitId)
        }
        selectedHabitIds.value = current
    }

    fun startReactionTest() {
        waitJob?.cancel()
        _reactionState.value = ReactionState.WAITING
        waitJob = viewModelScope.launch {
            val randomDelay = (1500..3500).random().toLong()
            delay(randomDelay)
            startTimeMs = System.currentTimeMillis()
            _reactionState.value = ReactionState.CLICK_NOW
        }
    }

    fun onReactionClicked() {
        when (_reactionState.value) {
            ReactionState.WAITING -> {
                waitJob?.cancel()
                _reactionState.value = ReactionState.TOO_EARLY
            }
            ReactionState.CLICK_NOW -> {
                val elapsed = System.currentTimeMillis() - startTimeMs
                _reactionTimeMs.value = elapsed
                _reactionState.value = ReactionState.FINISHED
            }
            ReactionState.TOO_EARLY, ReactionState.FINISHED, ReactionState.READY -> {
                startReactionTest()
            }
        }
    }

    fun submitCalibration(onSubmitted: () -> Unit) {
        viewModelScope.launch {
            repository.submitCheckIn(
                CheckInRequest(
                    date = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                    sleepQuality = sleepQuality.value,
                    stressLevel = stressLevel.value,
                    energyLevel = energyLevel.value,
                    mood = mood.value,
                    reactionTimeMs = _reactionTimeMs.value,
                    habitsCompleted = selectedHabitIds.value.toList()
                )
            )
            onSubmitted()
        }
    }
}

// ==========================================
// 5. AI CHAT VIEW MODEL
// ==========================================
class AiChatViewModel : ViewModel() {
    private val repository = NovaApplication.instance.novaRepository

    val chatMessages = repository.chatMessages

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            repository.sendChatMessage(text)
            _isSending.value = false
        }
    }
}
