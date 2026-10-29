package com.example.prayeralarm.domain.calculator

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.data.DateComponents
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.ui.home.PrayerScheduleItem
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

/**
 * Result of timetable calculation for a specific location and calendar date.
 *
 * @property items The five daily prayer timetable rows
 * @property prayerDateTimes Map of PrayerType to full ZonedDateTime in the location's timezone
 */
data class CalculatedTimetable(
    val items: List<PrayerScheduleItem>,
    val prayerDateTimes: Map<PrayerType, ZonedDateTime>
)

/**
 * Information regarding the upcoming prayer and remaining duration.
 */
data class NextPrayerCountdown(
    val nextPrayerItem: PrayerScheduleItem?,
    val nextPrayerDateTime: ZonedDateTime?,
    val remainingHours: Long,
    val remainingMinutes: Long,
    val remainingSeconds: Long,
    val isPastAllPrayersForDay: Boolean = false
)

/**
 * Clean astronomical calculation engine for Islamic prayer times.
 *
 * Implements authoritative Tunisian parameters ($18.0^\circ$ Fajr, $18.0^\circ$ Isha, Maliki Asr).
 * Uses Batoul Apps Adhan engine (Jean Meeus astronomical algorithms).
 */
object PrayerTimeCalculator {

    /**
     * Creates calculation parameters configured specifically for Tunisian standards.
     * Fajr: 18.0 degrees
     * Isha: 18.0 degrees
     * Asr: Madhab.SHAFI (shadow factor m=1, standard/Maliki juristic convention)
     */
    fun createTunisianParameters(): CalculationParameters {
        val params = CalculationMethod.OTHER.parameters
        params.fajrAngle = 18.0
        params.ishaAngle = 18.0
        params.madhab = Madhab.SHAFI
        return params
    }

    /**
     * Calculates the 5 daily prayer times for a given location, calendar date, and parameters.
     *
     * @param location SavedLocation with latitude, longitude, and IANA timezone ID
     * @param date LocalDate to calculate for
     * @param parameters CalculationParameters (defaults to Tunisian official conventions)
     * @return CalculatedTimetable containing prayer schedule items and exact ZonedDateTimes
     */
    fun calculateTimetable(
        location: SavedLocation,
        date: LocalDate,
        parameters: CalculationParameters = createTunisianParameters()
    ): CalculatedTimetable {
        val zoneId = try {
            ZoneId.of(location.timeZoneId)
        } catch (e: Exception) {
            ZoneId.of("Africa/Tunis")
        }

        val coordinates = Coordinates(location.latitude, location.longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)

        val adhanPrayerTimes = com.batoulapps.adhan.PrayerTimes(coordinates, dateComponents, parameters)

        fun dateToZonedDateTime(dateObj: Date?): ZonedDateTime {
            return if (dateObj != null) {
                dateObj.toInstant().atZone(zoneId)
            } else {
                // Fallback to approximate noon if extreme latitude anomaly
                date.atTime(12, 0).atZone(zoneId)
            }
        }

        val fajrZdt = dateToZonedDateTime(adhanPrayerTimes.fajr)
        val dhuhrZdt = dateToZonedDateTime(adhanPrayerTimes.dhuhr)
        val asrZdt = dateToZonedDateTime(adhanPrayerTimes.asr)
        val maghribZdt = dateToZonedDateTime(adhanPrayerTimes.maghrib)
        val ishaZdt = dateToZonedDateTime(adhanPrayerTimes.isha)

        val prayerMap = mapOf(
            PrayerType.FAJR to fajrZdt,
            PrayerType.DHUHR to dhuhrZdt,
            PrayerType.ASR to asrZdt,
            PrayerType.MAGHRIB to maghribZdt,
            PrayerType.ISHA to ishaZdt
        )

        val items = listOf(
            PrayerScheduleItem(type = PrayerType.FAJR, time = fajrZdt.toLocalTime()),
            PrayerScheduleItem(type = PrayerType.DHUHR, time = dhuhrZdt.toLocalTime()),
            PrayerScheduleItem(type = PrayerType.ASR, time = asrZdt.toLocalTime()),
            PrayerScheduleItem(type = PrayerType.MAGHRIB, time = maghribZdt.toLocalTime()),
            PrayerScheduleItem(type = PrayerType.ISHA, time = ishaZdt.toLocalTime())
        )

        return CalculatedTimetable(items = items, prayerDateTimes = prayerMap)
    }

    /**
     * Determines the next upcoming prayer and computes the exact remaining countdown duration.
     *
     * Source of truth is the difference between [now] and the target prayer's [ZonedDateTime].
     * If all prayers for [currentDate] have passed, automatically calculates tomorrow's Fajr
     * so the countdown seamlessly points to tomorrow morning's dawn prayer.
     *
     * @param location The active SavedLocation
     * @param currentDate The current calendar date in the location's timezone
     * @param now The current instant
     * @param todayTimetable The calculated timetable for today
     * @return NextPrayerCountdown with highlighted item, target timestamp, and remaining H:M:S
     */
    fun computeNextPrayerAndCountdown(
        location: SavedLocation,
        currentDate: LocalDate,
        now: Instant,
        todayTimetable: CalculatedTimetable,
        tomorrowTimetable: CalculatedTimetable? = null
    ): NextPrayerCountdown {
        val zoneId = try {
            ZoneId.of(location.timeZoneId)
        } catch (e: Exception) {
            ZoneId.of("Africa/Tunis")
        }

        // Ordered list of today's prayers
        val prayersToday = listOf(
            Pair(PrayerType.FAJR, todayTimetable.prayerDateTimes[PrayerType.FAJR]!!),
            Pair(PrayerType.DHUHR, todayTimetable.prayerDateTimes[PrayerType.DHUHR]!!),
            Pair(PrayerType.ASR, todayTimetable.prayerDateTimes[PrayerType.ASR]!!),
            Pair(PrayerType.MAGHRIB, todayTimetable.prayerDateTimes[PrayerType.MAGHRIB]!!),
            Pair(PrayerType.ISHA, todayTimetable.prayerDateTimes[PrayerType.ISHA]!!)
        )

        // Find the first prayer strictly in the future relative to 'now'
        val upcoming = prayersToday.firstOrNull { (_, zdt) ->
            zdt.toInstant().isAfter(now)
        }

        val targetPrayerType: PrayerType
        val targetZdt: ZonedDateTime
        val isTomorrowFajr: Boolean

        if (upcoming != null) {
            targetPrayerType = upcoming.first
            targetZdt = upcoming.second
            isTomorrowFajr = false
        } else {
            // All prayers for today have passed. Next prayer is tomorrow's Fajr!
            val tomorrow = currentDate.plusDays(1)
            val nextDayTimetable = tomorrowTimetable ?: calculateTimetable(location, tomorrow)
            targetPrayerType = PrayerType.FAJR
            targetZdt = nextDayTimetable.prayerDateTimes[PrayerType.FAJR]!!
            isTomorrowFajr = true
        }

        // Compute exact non-negative remaining seconds
        val targetInstant = targetZdt.toInstant()
        val totalSecondsRemaining = if (targetInstant.isAfter(now)) {
            java.time.Duration.between(now, targetInstant).seconds
        } else {
            0L
        }

        val hours = totalSecondsRemaining / 3600
        val minutes = (totalSecondsRemaining % 3600) / 60
        val seconds = totalSecondsRemaining % 60

        // Find the schedule item to highlight
        val highlightedItem = if (!isTomorrowFajr) {
            todayTimetable.items.find { it.type == targetPrayerType }?.copy(isNextPrayer = true)
        } else {
            // Highlight Fajr in today's list to denote upcoming dawn
            todayTimetable.items.find { it.type == PrayerType.FAJR }?.copy(
                time = targetZdt.toLocalTime(),
                isNextPrayer = true
            )
        }

        return NextPrayerCountdown(
            nextPrayerItem = highlightedItem,
            nextPrayerDateTime = targetZdt,
            remainingHours = hours,
            remainingMinutes = minutes,
            remainingSeconds = seconds,
            isPastAllPrayersForDay = isTomorrowFajr
        )
    }
}
