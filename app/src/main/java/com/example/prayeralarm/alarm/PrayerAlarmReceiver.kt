package com.example.prayeralarm.alarm

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.prayeralarm.MainActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.create(context)
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Selected location"
        val openAppIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.PRAYER_ALARM_CHANNEL
        )
            .setSmallIcon(com.example.prayeralarm.R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName prayer")
            .setContentText("Prayer time for $locationName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        NotificationManagerCompat.from(context).notify(prayerName.hashCode(), notification)
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        private const val NOTIFICATION_REQUEST_CODE = 1000
    }
}
