package com.example.prayeralarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayeralarm.PrayerAlarmApplication
import com.example.prayeralarm.data.notification.NotificationHelper
import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.domain.calculator.PrayerTimeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PrayerAlarmRecoveryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val application = context.applicationContext as PrayerAlarmApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reconcile(application)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun reconcile(application: PrayerAlarmApplication) {
        val location = application.locationRepository.getActiveLocation().first()
        val config = application.alarmPlanPreferences.config.first()
        if (location == null) {
            NotificationHelper.cancel(application)
            return
        }

        val zone = runCatching { ZoneId.of(location.timeZoneId) }
            .getOrElse { ZoneId.of("Africa/Tunis") }
        val clock = Clock.systemUTC()
        val today = LocalDate.now(clock.withZone(zone))
        val todayTimetable = PrayerTimeCalculator.calculateTimetable(location, today)
        val tomorrowTimetable =
            PrayerTimeCalculator.calculateTimetable(location, today.plusDays(1))
        val days = listOf(todayTimetable, tomorrowTimetable).map { timetable ->
            FajrScheduleDay(timetable.prayerDateTimes[PrayerType.FAJR]!!)
        }
        val coordinator = PrayerAlarmCoordinator(application.prayerAlarmScheduler)
        val existing = candidateAlarms(days, config, location.name, coordinator)
        coordinator.reconcile(days, config, Instant.now(clock), existing)

        if (!config.enabled) {
            NotificationHelper.cancel(application)
            return
        }

        val now = Instant.now(clock)
        val countdown = PrayerTimeCalculator.computeNextPrayerAndCountdown(
            location = location,
            currentDate = today,
            now = now,
            todayTimetable = todayTimetable,
            tomorrowTimetable = tomorrowTimetable
        )
        val remainingMillis = countdown.nextPrayerDateTime
            ?.let { Duration.between(now, it).toMillis() }
            ?: 0L
        NotificationHelper.postWhenEnabled(
            context = application,
            selectedLocation = location,
            nextPrayer = countdown.nextPrayerItem,
            remainingMillis = remainingMillis
        )
        CountdownNotificationService.start(application)
    }

    private fun candidateAlarms(
        days: List<FajrScheduleDay>,
        config: AlarmPlanConfig,
        locationName: String,
        coordinator: PrayerAlarmCoordinator
    ): List<ScheduledPrayerAlarm> {
        val enabledConfig = config.copy(enabled = true)
        return days.flatMap { day ->
            val generated = AlarmPlanGenerator.generate(
                day.fajrAt.toLocalDateTime(),
                enabledConfig
            )
            (generated as? com.example.prayeralarm.domain.alarm.AlarmPlanResult.Success)
                ?.alarms
                ?.map { alarm ->
                    val instant = alarm.scheduledAt.atZone(day.fajrAt.zone).toInstant()
                    ScheduledPrayerAlarm(
                        requestCode = coordinator.requestCodeFor(day, alarm.sequence),
                        triggerAtMillis = instant.toEpochMilli(),
                        prayerName = "Fajr",
                        locationName = locationName
                    )
                }
                .orEmpty()
        }
    }
}
