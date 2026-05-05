package com.deepseek.studycircle.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

data class FocusSession(
    val id: String,
    val circleId: String?,
    val subject: String,
    val durationMinutes: Int,
    val completed: Boolean,
    val productivityRating: Int,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TimerViewModel : ViewModel() {

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _totalMinutes = MutableStateFlow(25)
    val totalMinutes: StateFlow<Int> = _totalMinutes.asStateFlow()

    private val _selectedSubject = MutableStateFlow("")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedCircleId = MutableStateFlow<String?>(null)
    val selectedCircleId: StateFlow<String?> = _selectedCircleId.asStateFlow()

    private val _productivityRating = MutableStateFlow(0)
    val productivityRating: StateFlow<Int> = _productivityRating.asStateFlow()

    private val _sessionNotes = MutableStateFlow("")
    val sessionNotes: StateFlow<String> = _sessionNotes.asStateFlow()

    private var timerJob: Job? = null

    fun setDuration(minutes: Int) {
        if (_timerState.value == TimerState.IDLE) {
            _totalMinutes.value = minutes
            _remainingSeconds.value = minutes * 60
        }
    }

    fun setSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun setCircleId(circleId: String?) {
        _selectedCircleId.value = circleId
    }

    fun setProductivityRating(rating: Int) {
        _productivityRating.value = rating
    }

    fun setSessionNotes(notes: String) {
        _sessionNotes.value = notes
    }

    fun startTimer() {
        if (_timerState.value != TimerState.RUNNING) {
            _timerState.value = TimerState.RUNNING
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_remainingSeconds.value > 0 && _timerState.value == TimerState.RUNNING) {
                    delay(1000)
                    _remainingSeconds.value--
                }
                if (_remainingSeconds.value <= 0) {
                    completeSession()
                }
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
    }

    fun resumeTimer() {
        startTimer()
    }

    fun stopTimer() {
        _timerState.value = TimerState.IDLE
        timerJob?.cancel()
        _remainingSeconds.value = _totalMinutes.value * 60
    }

    fun completeSession() {
        _timerState.value = TimerState.COMPLETED
        timerJob?.cancel()
    }

    fun saveSession() {
        val session = FocusSession(
            id = System.currentTimeMillis().toString(),
            circleId = _selectedCircleId.value,
            subject = _selectedSubject.value.ifEmpty { "General Study" },
            durationMinutes = _totalMinutes.value - (_remainingSeconds.value / 60),
            completed = true,
            productivityRating = _productivityRating.value,
            notes = _sessionNotes.value
        )
        // Here you would typically save to a database or API
        resetTimer()
    }

    fun resetTimer() {
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = _totalMinutes.value * 60
        _productivityRating.value = 0
        _sessionNotes.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
