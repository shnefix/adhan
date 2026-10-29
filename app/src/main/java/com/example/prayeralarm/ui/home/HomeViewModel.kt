package com.example.prayeralarm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.domain.calculator.CalculatedTimetable
import com.example.prayeralarm.domain.calculator.PrayerTimeCalculator
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel managing Home Screen prayer timetable and real-time countdown.
 *
 * Implements real astronomical calculation based on the active [SavedLocation].
 * Uses an injectable [Clock] for deterministic testing.
 */
class HomeViewModel(
    private val locationRepository: LocationRepository? = null,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var activeLocation: SavedLocation? = null
    private var cachedTimetable: CalculatedTimetable? = null
    private var cachedTimetableDate: LocalDate? = null
    private var cachedTomorrowTimetable: CalculatedTimetable? = null

    init {
        observeActiveLocation()
        startCountdownTicker()
    }

    private fun observeActiveLocation() {
        locationRepository?.let { repo ->
            viewModelScope.launch {
                repo.getActiveLocation().collect { location ->
                    if (location != null) {
                        activeLocation = location
                        recalculateTimetable(location)
                    } else {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                errorMessage = "No active location available."
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Recalculates prayer times for the provided location and updates the countdown.
     * Avoids recalculating table values on every countdown tick by caching the day's timetable.
     */
    private fun recalculateTimetable(location: SavedLocation) {
        val zoneId = try {
            ZoneId.of(location.timeZoneId)
        } catch (e: Exception) {
            ZoneId.of("Africa/Tunis")
        }

        val today = LocalDate.now(clock.withZone(zoneId))
        val timetable = PrayerTimeCalculator.calculateTimetable(location, today)
        cachedTimetable = timetable
        cachedTimetableDate = today
        cachedTomorrowTimetable = PrayerTimeCalculator.calculateTimetable(location, today.plusDays(1))

        val now = Instant.now(clock)
        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = location,
            currentDate = today,
            now = now,
            todayTimetable = timetable,
            tomorrowTimetable = cachedTomorrowTimetable
        )

        val updatedItems = timetable.items.map { item ->
            item.copy(isNextPrayer = (item.type == countdown.nextPrayerItem?.type))
        }

        _uiState.update { current ->
            current.copy(
                selectedLocation = location,
                currentDate = today,
                prayerTimetable = updatedItems,
                nextPrayer = countdown.nextPrayerItem,
                remainingHours = countdown.remainingHours,
                remainingMinutes = countdown.remainingMinutes,
                remainingSeconds = countdown.remainingSeconds,
                isSampleData = false,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    /**
     * Precise ticker running once per second.
     * Only updates the countdown and next-prayer pointer against pre-calculated timestamps.
     * If midnight passes (date changes), triggers a full timetable recalculation.
     */
    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                tickCountdown()
            }
        }
    }

    /**
     * Advances the countdown by comparing the current instant with the upcoming prayer's timestamp.
     */
    fun tickCountdown() {
        val location = activeLocation ?: return
        val timetable = cachedTimetable ?: return
        val timetableDate = cachedTimetableDate ?: return

        val zoneId = try {
            ZoneId.of(location.timeZoneId)
        } catch (e: Exception) {
            ZoneId.of("Africa/Tunis")
        }

        val currentDate = LocalDate.now(clock.withZone(zoneId))

        // If the day has rolled over at midnight, recalculate today's timetable!
        if (currentDate != timetableDate) {
            recalculateTimetable(location)
            return
        }

        val now = Instant.now(clock)
        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = location,
            currentDate = currentDate,
            now = now,
            todayTimetable = timetable,
            tomorrowTimetable = cachedTomorrowTimetable
        )

        val updatedItems = timetable.items.map { item ->
            item.copy(isNextPrayer = (item.type == countdown.nextPrayerItem?.type))
        }

        _uiState.update { current ->
            current.copy(
                nextPrayer = countdown.nextPrayerItem,
                prayerTimetable = updatedItems,
                remainingHours = countdown.remainingHours,
                remainingMinutes = countdown.remainingMinutes,
                remainingSeconds = countdown.remainingSeconds
            )
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

    class Factory(
        private val repository: LocationRepository,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository, clock) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Sample state strictly for Compose @Preview rendering in IDE.
         */
        fun createPreviewState(): HomeUiState {
            val sampleLocation = SavedLocation(
                id = 1,
                name = "Mahdia (Preview)",
                latitude = 35.5047,
                longitude = 11.0622,
                timeZoneId = "Africa/Tunis",
                isDefault = true
            )
            val timetable = PrayerTimeCalculator.calculateTimetable(sampleLocation, LocalDate.now())
            return HomeUiState(
                selectedLocation = sampleLocation,
                currentDate = LocalDate.now(),
                hijriDate = "18 Jumada al-Awwal 1448",
                nextPrayer = timetable.items[1],
                remainingHours = 2,
                remainingMinutes = 15,
                remainingSeconds = 30,
                prayerTimetable = timetable.items,
                isSampleData = true
            )
        }
    }
}
