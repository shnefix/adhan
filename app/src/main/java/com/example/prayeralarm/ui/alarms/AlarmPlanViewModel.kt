package com.example.prayeralarm.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayeralarm.data.preferences.UserPreferencesManager
import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlarmPlanViewModel(
    private val preferences: AlarmPlanPreferences
) : ViewModel() {

    private val _config = MutableStateFlow(AlarmPlanConfig.DEFAULT)
    val config: StateFlow<AlarmPlanConfig> = _config.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.config
                .catch { error -> _errorMessage.value = error.message ?: "Unable to load alarm settings." }
                .collect { _config.value = it }
        }
    }

    fun setEnabled(enabled: Boolean) = update { it.copy(enabled = enabled) }

    fun setAlarmCount(count: Int) = update { it.copy(alarmCount = count) }

    fun setIntervalMinutes(interval: Long) = update { it.copy(intervalMinutes = interval) }

    fun setTimingMode(mode: AlarmTimingMode) = update { it.copy(timingMode = mode) }

    private fun update(transform: (AlarmPlanConfig) -> AlarmPlanConfig) {
        val updated = transform(_config.value)
        if (updated.alarmCount < 1 || updated.intervalMinutes < 1) {
            _errorMessage.value = "Alarm count and interval must be positive."
            return
        }
        _errorMessage.value = null
        _config.value = updated
        viewModelScope.launch {
            runCatching { preferences.setConfig(updated) }
                .onFailure { _errorMessage.value = it.message ?: "Unable to save alarm settings." }
        }
    }

    class Factory(private val preferences: AlarmPlanPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlarmPlanViewModel::class.java)) {
                return AlarmPlanViewModel(preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

interface AlarmPlanPreferences {
    val config: kotlinx.coroutines.flow.Flow<AlarmPlanConfig>
    suspend fun setConfig(config: AlarmPlanConfig)
}

class DataStoreAlarmPlanPreferences(
    private val manager: UserPreferencesManager
) : AlarmPlanPreferences {
    override val config = manager.alarmPlanConfig
    override suspend fun setConfig(config: AlarmPlanConfig) {
        manager.setAlarmPlanConfig(config)
    }
}
