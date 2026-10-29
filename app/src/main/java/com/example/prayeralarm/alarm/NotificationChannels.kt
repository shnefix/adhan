package com.example.prayeralarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val PRAYER_ALARM_CHANNEL = "prayer_alarm"
    const val PRAYER_COUNTDOWN_CHANNEL = "prayer_countdown"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val alarmChannel = NotificationChannel(
            PRAYER_ALARM_CHANNEL,
            "Prayer alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for scheduled prayer alarms."
        }

        val countdownChannel = NotificationChannel(
            PRAYER_COUNTDOWN_CHANNEL,
            "Next prayer countdown",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent countdown to the next prayer."
            setShowBadge(false)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(listOf(alarmChannel, countdownChannel))
    }
}
