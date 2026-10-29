package com.example.prayeralarm.domain.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AlarmPlanGeneratorTest {

    private val fajr = LocalDateTime.of(2026, 10, 29, 5, 0)

    @Test
    fun bothMode_matchesFiveAlarmExample() {
        val result = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(5, 10, AlarmTimingMode.BOTH)
        )

        assertEquals(
            listOf("04:20", "04:30", "04:40", "04:50", "05:00"),
            (result as AlarmPlanResult.Success).alarms.map { it.scheduledAt.toLocalTime().toString() }
        )
    }

    @Test
    fun beforeMode_generatesAlarmsEndingAtFajr() {
        val result = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(3, 15, AlarmTimingMode.BEFORE_FAJR)
        ) as AlarmPlanResult.Success

        assertEquals(fajr.minusMinutes(30), result.alarms.first().scheduledAt)
        assertEquals(fajr, result.alarms.last().scheduledAt)
    }

    @Test
    fun atOrAfterMode_generatesAlarmsStartingAtFajr() {
        val result = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(3, 15, AlarmTimingMode.AT_OR_AFTER_FAJR)
        ) as AlarmPlanResult.Success

        assertEquals(fajr, result.alarms.first().scheduledAt)
        assertEquals(fajr.plusMinutes(30), result.alarms.last().scheduledAt)
    }

    @Test
    fun invalidCountAndInterval_areRejected() {
        val zeroCount = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(0, 10, AlarmTimingMode.BEFORE_FAJR)
        )
        val zeroInterval = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(2, 0, AlarmTimingMode.BEFORE_FAJR)
        )

        assertTrue(zeroCount is AlarmPlanResult.Invalid)
        assertTrue(zeroInterval is AlarmPlanResult.Invalid)
    }

    @Test
    fun disabledPlan_isEmpty() {
        val result = AlarmPlanGenerator.generate(
            fajr,
            AlarmPlanConfig(5, 10, AlarmTimingMode.BOTH, enabled = false)
        )

        assertEquals(emptyList<PlannedAlarm>(), (result as AlarmPlanResult.Success).alarms)
    }

    @Test
    fun scheduleCanCrossMidnight_withoutLosingDate() {
        val midnightFajr = LocalDateTime.of(2026, 10, 29, 0, 10)
        val result = AlarmPlanGenerator.generate(
            midnightFajr,
            AlarmPlanConfig(3, 15, AlarmTimingMode.BEFORE_FAJR)
        ) as AlarmPlanResult.Success

        assertEquals(LocalDateTime.of(2026, 10, 28, 23, 40), result.alarms.first().scheduledAt)
        assertEquals(30L, AlarmPlanGenerator.durationBetweenFirstAndLast(result.alarms))
    }
}
