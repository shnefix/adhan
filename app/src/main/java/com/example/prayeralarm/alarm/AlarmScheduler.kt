package com.example.prayeralarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

data class ScheduledPrayerAlarm(
    val requestCode: Int,
    val triggerAtMillis: Long,
    val prayerName: String,
    val locationName: String
)

sealed interface AlarmScheduleResult {
    data object Scheduled : AlarmScheduleResult
    data object Cancelled : AlarmScheduleResult
    data object ExactAlarmPermissionRequired : AlarmScheduleResult
    data class Invalid(val message: String) : AlarmScheduleResult
}

interface PrayerAlarmScheduler {
    fun schedule(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult
    fun cancel(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult
}

class AndroidAlarmScheduler(context: Context) : PrayerAlarmScheduler {

    private val applicationContext = context.applicationContext
    private val alarmManager =
        applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult {
        if (alarms.any { it.triggerAtMillis <= 0L || it.requestCode < 0 }) {
            return AlarmScheduleResult.Invalid("Alarm identity and trigger time must be positive.")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            return AlarmScheduleResult.ExactAlarmPermissionRequired
        }

        return try {
            alarms.forEach { alarm ->
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerAtMillis,
                    pendingIntent(alarm)
                )
            }
            AlarmScheduleResult.Scheduled
        } catch (securityException: SecurityException) {
            AlarmScheduleResult.Invalid(
                securityException.message ?: "Android rejected exact alarm scheduling."
            )
        }
    }

    override fun cancel(alarms: List<ScheduledPrayerAlarm>): AlarmScheduleResult {
        alarms.forEach { alarm ->
            alarmManager.cancel(pendingIntent(alarm))
        }
        return AlarmScheduleResult.Cancelled
    }

    private fun pendingIntent(alarm: ScheduledPrayerAlarm): PendingIntent {
        val intent = Intent(applicationContext, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, alarm.prayerName)
            putExtra(PrayerAlarmReceiver.EXTRA_LOCATION_NAME, alarm.locationName)
        }
        return PendingIntent.getBroadcast(
            applicationContext,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
