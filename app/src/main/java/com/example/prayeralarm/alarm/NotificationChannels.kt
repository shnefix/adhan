package com.example.prayeralarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val PRAYER_ALARM_CHANNEL = "prayer_alarm"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            PRAYER_ALARM_CHANNEL,
            "Prayer alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for scheduled prayer alarms."
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
