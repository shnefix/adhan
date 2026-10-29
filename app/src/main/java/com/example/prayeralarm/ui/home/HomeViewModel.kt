package com.example.prayeralarm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * ViewModel responsible for managing Home Screen UI state and live countdown updates.
 *
 * In Stage 3, this ViewModel observes the active [SavedLocation] from [LocationRepository],
 * updating the displayed location name dynamically whenever the active selection changes.
 *
 * NOTE: Timetable times remain explicit mock preview data until Stage 4.
 */
class HomeViewModel(
    private val locationRepository: LocationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialSampleState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        startMockCountdown()
        observeActiveLocation()
    }

    private fun observeActiveLocation() {
        locationRepository?.let { repo ->
            viewModelScope.launch {
                repo.getActiveLocation().collect { activeLocation ->
                    if (activeLocation != null) {
                        _uiState.update { current ->
                            current.copy(selectedLocation = activeLocation)
                        }
                    }
                }
            }
        }
    }

    private fun startMockCountdown() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { current ->
                    var seconds = current.remainingSeconds - 1
                    var minutes = current.remainingMinutes
                    var hours = current.remainingHours

                    if (seconds < 0) {
                        seconds = 59
                        minutes -= 1
                    }
                    if (minutes < 0) {
                        minutes = 59
                        hours -= 1
                    }
                    if (hours < 0) {
                        // Reset mock countdown loop for preview
                        hours = 3
                        minutes = 45
                        seconds = 0
                    }

                    current.copy(
                        remainingHours = hours,
                        remainingMinutes = minutes,
                        remainingSeconds = seconds
                    )
                }
            }
        }
    }

    fun togglePrayerNotification(prayerType: PrayerType) {
        _uiState.update { state ->
            val updated = state.prayerTimetable.map { item ->
                if (item.type == prayerType) {
                    item.copy(isNotificationEnabled = !item.isNotificationEnabled)
                } else {
                    item
                }
            }
            state.copy(prayerTimetable = updated)
        }
    }

    class Factory(private val repository: LocationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Creates explicit representative sample data for preview and default state.
         */
        fun createInitialSampleState(): HomeUiState {
            val sampleItems = listOf(
                PrayerScheduleItem(type = PrayerType.FAJR, time = LocalTime.of(5, 12), isNextPrayer = false),
                PrayerScheduleItem(type = PrayerType.DHUHR, time = LocalTime.of(12, 16), isNextPrayer = true),
                PrayerScheduleItem(type = PrayerType.ASR, time = LocalTime.of(15, 14), isNextPrayer = false),
                PrayerScheduleItem(type = PrayerType.MAGHRIB, time = LocalTime.of(17, 30), isNextPrayer = false),
                PrayerScheduleItem(type = PrayerType.ISHA, time = LocalTime.of(18, 55), isNextPrayer = false)
            )

            return HomeUiState(
                selectedLocation = SavedLocation(
                    id = 1,
                    name = "Tunis (Sample)",
                    latitude = 36.8065,
                    longitude = 10.1815,
                    timeZoneId = "Africa/Tunis",
                    isDefault = true
                ),
                currentDate = LocalDate.now(),
                hijriDate = "18 Jumada al-Awwal 1448",
                nextPrayer = sampleItems[1], // Dhuhr as example next prayer
                remainingHours = 2,
                remainingMinutes = 14,
                remainingSeconds = 30,
                prayerTimetable = sampleItems,
                isSampleData = true
            )
        }
    }
}
