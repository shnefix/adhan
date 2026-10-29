package com.example.prayeralarm.domain.calculator

import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Deterministic unit tests for the prayer time calculation engine and countdown calculations.
 */
class PrayerTimeCalculatorTest {

    private val mahdia = SavedLocation(
        name = "Mahdia",
        latitude = 35.5047,
        longitude = 11.0622,
        timeZoneId = "Africa/Tunis"
    )

    private val kelibia = SavedLocation(
        name = "Kelibia",
        latitude = 36.8476,
        longitude = 11.0939,
        timeZoneId = "Africa/Tunis"
    )

    private val ariana = SavedLocation(
        name = "Ariana",
        latitude = 36.8625,
        longitude = 10.1956,
        timeZoneId = "Africa/Tunis"
    )

    private val testDate = LocalDate.of(2026, 10, 29)

    @Test
    fun calculateTimetable_producesChronologicalPrayers() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)

        assertEquals(5, timetable.items.size)

        val fajr = timetable.prayerDateTimes[PrayerType.FAJR]!!
        val dhuhr = timetable.prayerDateTimes[PrayerType.DHUHR]!!
        val asr = timetable.prayerDateTimes[PrayerType.ASR]!!
        val maghrib = timetable.prayerDateTimes[PrayerType.MAGHRIB]!!
        val isha = timetable.prayerDateTimes[PrayerType.ISHA]!!

        assertTrue("Fajr must precede Dhuhr", fajr.isBefore(dhuhr))
        assertTrue("Dhuhr must precede Asr", dhuhr.isBefore(asr))
        assertTrue("Asr must precede Maghrib", asr.isBefore(maghrib))
        assertTrue("Maghrib must precede Isha", maghrib.isBefore(isha))
    }

    @Test
    fun differentLocations_produceDistinctPrayerTimes() {
        val mahdiaTimetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val arianaTimetable = PrayerTimeCalculator.calculateTimetable(ariana, testDate)

        val mahdiaMaghrib = mahdiaTimetable.items.find { it.type == PrayerType.MAGHRIB }!!.time
        val arianaMaghrib = arianaTimetable.items.find { it.type == PrayerType.MAGHRIB }!!.time

        // Ariana (10.1956° E) is west of Mahdia (11.0622° E), so sunset occurs later in Ariana
        assertNotEquals(
            "Different coordinates must produce different prayer times",
            mahdiaMaghrib,
            arianaMaghrib
        )
        assertTrue(
            "Ariana Maghrib must occur after Mahdia Maghrib",
            arianaMaghrib.isAfter(mahdiaMaghrib)
        )
    }

    @Test
    fun computeNextPrayerAndCountdown_beforeFajr() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val fajrZdt = timetable.prayerDateTimes[PrayerType.FAJR]!!

        // Set 'now' to 1 hour and 30 minutes before Fajr
        val now = fajrZdt.minusHours(1).minusMinutes(30).toInstant()

        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = mahdia,
            currentDate = testDate,
            now = now,
            todayTimetable = timetable
        )

        assertNotNull(countdown.nextPrayerItem)
        assertEquals(PrayerType.FAJR, countdown.nextPrayerItem?.type)
        assertEquals(1L, countdown.remainingHours)
        assertEquals(30L, countdown.remainingMinutes)
        assertEquals(0L, countdown.remainingSeconds)
        assertFalse(countdown.isPastAllPrayersForDay)
    }

    @Test
    fun computeNextPrayerAndCountdown_betweenDhuhrAndAsr() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val asrZdt = timetable.prayerDateTimes[PrayerType.ASR]!!

        // Set 'now' to exactly 45 minutes and 15 seconds before Asr
        val now = asrZdt.minusMinutes(45).minusSeconds(15).toInstant()

        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = mahdia,
            currentDate = testDate,
            now = now,
            todayTimetable = timetable
        )

        assertEquals(PrayerType.ASR, countdown.nextPrayerItem?.type)
        assertEquals(0L, countdown.remainingHours)
        assertEquals(45L, countdown.remainingMinutes)
        assertEquals(15L, countdown.remainingSeconds)
    }

    @Test
    fun computeNextPrayerAndCountdown_lessThanOneMinute_preservesSeconds() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val fajrZdt = timetable.prayerDateTimes[PrayerType.FAJR]!!
        val now = fajrZdt.minusSeconds(59).toInstant()

        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = mahdia,
            currentDate = testDate,
            now = now,
            todayTimetable = timetable
        )

        assertEquals(0L, countdown.remainingHours)
        assertEquals(0L, countdown.remainingMinutes)
        assertEquals(59L, countdown.remainingSeconds)
    }

    @Test
    fun computeNextPrayerAndCountdown_atPrayerBoundary_advancesToNextPrayer() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val dhuhrZdt = timetable.prayerDateTimes[PrayerType.DHUHR]!!
        val now = dhuhrZdt.toInstant()

        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = mahdia,
            currentDate = testDate,
            now = now,
            todayTimetable = timetable
        )

        assertEquals(PrayerType.ASR, countdown.nextPrayerItem?.type)
        assertTrue(countdown.remainingHours >= 0)
    }

    @Test
    fun computeNextPrayerAndCountdown_afterIsha_rollsOverToTomorrowFajr() {
        val timetable = PrayerTimeCalculator.calculateTimetable(mahdia, testDate)
        val ishaZdt = timetable.prayerDateTimes[PrayerType.ISHA]!!

        // Set 'now' to 10 minutes after Isha
        val now = ishaZdt.plusMinutes(10).toInstant()

        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = mahdia,
            currentDate = testDate,
            now = now,
            todayTimetable = timetable
        )

        // After Isha, next prayer is Fajr tomorrow
        assertEquals(PrayerType.FAJR, countdown.nextPrayerItem?.type)
        assertTrue(countdown.isPastAllPrayersForDay)
        assertTrue(countdown.remainingHours > 0)
        val tomorrowFajr = PrayerTimeCalculator.calculateTimetable(mahdia, testDate.plusDays(1))
            .prayerDateTimes[PrayerType.FAJR]!!.toLocalTime()
        assertEquals(tomorrowFajr.hour, countdown.nextPrayerItem?.time?.hour)
        assertEquals(tomorrowFajr.minute, countdown.nextPrayerItem?.time?.minute)
        assertEquals(tomorrowFajr.second, countdown.nextPrayerItem?.time?.second)
    }
}
