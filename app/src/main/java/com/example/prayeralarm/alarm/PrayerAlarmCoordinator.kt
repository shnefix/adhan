package com.example.prayeralarm.alarm

import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import java.time.Instant
import java.time.ZonedDateTime

data class FajrScheduleDay(
    val fajrAt: ZonedDateTime
)

sealed interface AlarmReconciliationResult {
    data class Applied(
        val desired: List<ScheduledPrayerAlarm>,
        val cancelled: List<ScheduledPrayerAlarm>
    ) : AlarmReconciliationResult

    data class Failed(val reason: AlarmScheduleResult) : AlarmReconciliationResult
}

class PrayerAlarmCoordinator(
    private val scheduler: PrayerAlarmScheduler
) {
    fun requestCodeFor(day: FajrScheduleDay, sequence: Int): Int {
        return stableRequestCode(day.fajrAt, sequence)
    }

    fun reconcile(
        days: List<FajrScheduleDay>,
        config: AlarmPlanConfig,
        now: Instant,
        existing: List<ScheduledPrayerAlarm>
    ): AlarmReconciliationResult {
        val desired = if (config.enabled) {
            days.flatMap { day ->
                when (val result = AlarmPlanGenerator.generate(
                    day.fajrAt.toLocalDateTime(),
                    config
                )) {
                    is com.example.prayeralarm.domain.alarm.AlarmPlanResult.Success -> {
                        result.alarms.mapNotNull { alarm ->
                            val instant = alarm.scheduledAt.atZone(day.fajrAt.zone).toInstant()
                            if (!instant.isAfter(now)) {
                                null
                            } else {
                                ScheduledPrayerAlarm(
                                    requestCode = stableRequestCode(day.fajrAt, alarm.sequence),
                                    triggerAtMillis = instant.toEpochMilli(),
                                    prayerName = "Fajr",
                                    locationName = "Selected location"
                                )
                            }
                        }
                    }

                    is com.example.prayeralarm.domain.alarm.AlarmPlanResult.Invalid -> {
                        return AlarmReconciliationResult.Failed(
                            AlarmScheduleResult.Invalid(result.message)
                        )
                    }
                }
            }.distinctBy { it.requestCode }
        } else {
            emptyList()
        }

        val desiredIds = desired.map { it.requestCode }.toSet()
        val obsolete = existing.filter { it.requestCode !in desiredIds }
        val cancelResult = scheduler.cancel(obsolete)
        if (cancelResult !is AlarmScheduleResult.Cancelled) {
            return AlarmReconciliationResult.Failed(cancelResult)
        }

        val scheduleResult = scheduler.schedule(desired)
        if (scheduleResult !is AlarmScheduleResult.Scheduled) {
            return AlarmReconciliationResult.Failed(scheduleResult)
        }
        return AlarmReconciliationResult.Applied(desired, obsolete)
    }

    private fun stableRequestCode(fajrAt: ZonedDateTime, sequence: Int): Int {
        return Math.floorMod(
            (fajrAt.toLocalDate().toEpochDay() * 31L + sequence).toInt(),
            Int.MAX_VALUE
        )
    }
}
