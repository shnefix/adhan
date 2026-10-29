package com.example.prayeralarm.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.ui.alarms.AlarmPlanPreferences
import com.example.prayeralarm.domain.calculator.PrayerTimeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.domain.model.PrayerType

class PrayerAlarmSchedulingViewModel(
    locationRepository: LocationRepository,
    alarmPreferences: AlarmPlanPreferences,
    private val scheduler: PrayerAlarmScheduler,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _status = MutableStateFlow<AlarmReconciliationResult?>(null)
    val status: StateFlow<AlarmReconciliationResult?> = _status.asStateFlow()

    private var existing = emptyList<ScheduledPrayerAlarm>()

    init {
        viewModelScope.launch {
            combine(
                locationRepository.getActiveLocation(),
                alarmPreferences.config
            ) { location, config -> location to config }
                .catch { error ->
                    _status.value = AlarmReconciliationResult.Failed(
                        AlarmScheduleResult.Invalid(error.message ?: "Unable to load alarm inputs.")
                    )
                }
                .collectLatest { (location, config) ->
                    if (location == null) {
                        existing = emptyList()
                        _status.value = AlarmReconciliationResult.Applied(emptyList(), emptyList())
                    } else {
                        reconcile(location, config)
                    }
                }
        }
    }

    private fun reconcile(
        location: SavedLocation,
        config: com.example.prayeralarm.domain.alarm.AlarmPlanConfig
    ) {
        val timeZoneId = location.timeZoneId
        val locationName = location.name
        val zone = runCatching { ZoneId.of(timeZoneId) }.getOrElse { ZoneId.of("Africa/Tunis") }
        val today = LocalDate.now(clock.withZone(zone))
        val days = listOf(today, today.plusDays(1)).map { date ->
            FajrScheduleDay(
                PrayerTimeCalculator.calculateTimetable(
                    SavedLocation(
                        name = locationName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timeZoneId = timeZoneId
                    ),
                    date
                ).prayerDateTimes[PrayerType.FAJR]!!
            )
        }
        val result = PrayerAlarmCoordinator(scheduler).reconcile(
            days = days,
            config = config,
            now = Instant.now(clock),
            existing = existing.map { it.copy(locationName = locationName) }
        )
        _status.value = result
        if (result is AlarmReconciliationResult.Applied) {
            existing = result.desired
        }
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val alarmPreferences: AlarmPlanPreferences,
        private val scheduler: PrayerAlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PrayerAlarmSchedulingViewModel::class.java)) {
                return PrayerAlarmSchedulingViewModel(
                    locationRepository,
                    alarmPreferences,
                    scheduler
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
