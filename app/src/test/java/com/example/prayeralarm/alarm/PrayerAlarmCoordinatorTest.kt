package com.example.prayeralarm.alarm

import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerAlarmCoordinatorTest {
    private val zone = ZoneId.of("Africa/Tunis")
    private val now = ZonedDateTime.of(2026, 10, 29, 1, 0, 0, 0, zone).toInstant()
    private val config = AlarmPlanConfig(2, 10, AlarmTimingMode.BEFORE_FAJR, enabled = true)

    @Test
    fun reconcile_schedulesFutureAlarmsAndCancelsObsoleteEntries() {
        val old = ScheduledPrayerAlarm(999, now.plusSeconds(60).toEpochMilli(), "Fajr", "Old")
        val scheduler = FakeScheduler()
        val result = PrayerAlarmCoordinator(scheduler).reconcile(
            days = listOf(FajrScheduleDay(ZonedDateTime.of(2026, 10, 29, 5, 0, 0, 0, zone))),
            config = config,
            now = now,
            existing = listOf(old)
        )

        val applied = result as AlarmReconciliationResult.Applied
        assertEquals(2, applied.desired.size)
        assertEquals(listOf(old), applied.cancelled)
        assertEquals(2, scheduler.scheduled.size)
    }

    @Test
    fun reconcile_isIdempotentForSameInputs() {
        val scheduler = FakeScheduler()
        val coordinator = PrayerAlarmCoordinator(scheduler)
        val day = FajrScheduleDay(ZonedDateTime.of(2026, 10, 29, 5, 0, 0, 0, zone))
        val first = coordinator.reconcile(listOf(day), config, now, emptyList())
        val desired = (first as AlarmReconciliationResult.Applied).desired

        val second = coordinator.reconcile(listOf(day), config, now, desired)

        assertTrue((second as AlarmReconciliationResult.Applied).cancelled.isEmpty())
        assertEquals(4, scheduler.scheduled.size)
    }

    @Test
    fun disabledPlan_cancelsEverythingAndSchedulesNothing() {
        val scheduler = FakeScheduler()
        val existing = ScheduledPrayerAlarm(1, now.plusSeconds(60).toEpochMilli(), "Fajr", "Selected")

        val result = PrayerAlarmCoordinator(scheduler).reconcile(
            days = emptyList(),
            config = config.copy(enabled = false),
            now = now,
            existing = listOf(existing)
        )

        val applied = result as AlarmReconciliationResult.Applied
        assertTrue(applied.desired.isEmpty())
        assertEquals(listOf(existing), applied.cancelled)
    }
}

private class FakeScheduler : PrayerAlarmScheduler {
    val scheduled = mutableListOf<ScheduledPrayerAlarm>()
    override fun schedule(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult {
        scheduled += alarms
        return AlarmScheduleResult.Scheduled
    }

    override fun cancel(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult {
        scheduled.removeAll(alarms.toSet())
        return AlarmScheduleResult.Cancelled
    }
}
