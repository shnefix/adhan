package com.example.prayeralarm.domain.alarm

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class AlarmTimingMode {
    BEFORE_FAJR,
    AT_OR_AFTER_FAJR,
    BOTH
}

data class AlarmPlanConfig(
    val alarmCount: Int,
    val intervalMinutes: Long,
    val timingMode: AlarmTimingMode,
    val enabled: Boolean = true
)

data class PlannedAlarm(
    val sequence: Int,
    val scheduledAt: LocalDateTime
)

sealed interface AlarmPlanResult {
    data class Success(val alarms: List<PlannedAlarm>) : AlarmPlanResult
    data class Invalid(val message: String) : AlarmPlanResult
}

object AlarmPlanGenerator {

    fun generate(
        fajrAt: LocalDateTime,
        config: AlarmPlanConfig
    ): AlarmPlanResult {
        if (!config.enabled) {
            return AlarmPlanResult.Success(emptyList())
        }
        if (config.alarmCount < 1) {
            return AlarmPlanResult.Invalid("Alarm count must be at least one.")
        }
        if (config.intervalMinutes < 1) {
            return AlarmPlanResult.Invalid("Alarm interval must be at least one minute.")
        }

        val timestamps = when (config.timingMode) {
            AlarmTimingMode.BEFORE_FAJR -> {
                (config.alarmCount - 1 downTo 0).map { offset ->
                    fajrAt.minusMinutes(offset * config.intervalMinutes)
                }
            }

            AlarmTimingMode.AT_OR_AFTER_FAJR -> {
                (0 until config.alarmCount).map { offset ->
                    fajrAt.plusMinutes(offset * config.intervalMinutes)
                }
            }

            AlarmTimingMode.BOTH -> {
                (config.alarmCount - 1 downTo 0).map { offset ->
                    fajrAt.minusMinutes(offset * config.intervalMinutes)
                }
            }
        }.distinct()

        if (timestamps.size != config.alarmCount) {
            return AlarmPlanResult.Invalid("Alarm configuration produced duplicate timestamps.")
        }

        return AlarmPlanResult.Success(
            timestamps.mapIndexed { index, timestamp ->
                PlannedAlarm(sequence = index + 1, scheduledAt = timestamp)
            }
        )
    }

    fun durationBetweenFirstAndLast(alarms: List<PlannedAlarm>): Long {
        if (alarms.size < 2) return 0L
        return ChronoUnit.MINUTES.between(alarms.first().scheduledAt, alarms.last().scheduledAt)
    }
}
