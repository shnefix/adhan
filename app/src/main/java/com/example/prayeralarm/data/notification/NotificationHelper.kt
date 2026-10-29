package com.example.prayeralarm.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.prayeralarm.alarm.NotificationChannels
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.ui.home.HomeUiState
import com.example.prayeralarm.ui.home.PrayerScheduleItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val NOTIFICATION_ID_NEXT_PRAYER = 12345

/**
 * Handles posting, updating, and canceling the persistent "next prayer" notification.
 * Uses a stable notification ID so updates replace the existing notification instead
 * of creating duplicates.
 *
 * Integration points:
 * - Call [postWhenEnabled] when the alarm plan becomes enabled
 * - Call [updateCountdown] from [HomeViewModel.tickCountdown()] each second
 * - Call [cancel] when the alarm plan is disabled or the app shuts down
 * - Call [restore] from [PrayerAlarmRecoveryReceiver] after reboot/etc.
 */
object NotificationHelper {

    private lateinit var manager: NotificationManagerCompat

    /** Must be called once during Application.onCreate. */
    fun init(context: Context) {
        manager = NotificationManagerCompat.from(context)
        NotificationChannels.create(context)
    }

    /** Post the notification when the alarm plan becomes enabled. */
    fun postWhenEnabled(
        context: Context,
        selectedLocation: SavedLocation,
        nextPrayer: PrayerScheduleItem?,
        remainingMillis: Long
    ) {
        if (!isNotificationEnabled(context)) return

        val prayerName = nextPrayer?.type.displayName ?: "Prayer"
        val prayerTime = nextPrayer?.time ?: Instant.EPOCH
        val formattedTime = dateTimeFormatter.format(prayerTime.atZone(selectedLocation.timeZoneId))
        val remaining = formatDuration(remainingMillis)

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_ALARM_CHANNEL).apply {
            setSmallIcon(com.example.prayeralarm.R.drawable.ic_launcher_foreground)
            setContentTitle("Next prayer: $prayerName")
            setContentText(
                if (remainingMillis > 0L) {
                    "Time: $formattedTime\nRemaining: $remaining"
                } else {
                    "Time: $formattedTime\nRemaining: 00:00:00"
                }
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(false)
            setOngoing(true)
        }

        manager.notify(NOTIFICATION_ID_NEXT_PRAYER, builder.build())
    }

    /** Update the notification content (replaces the existing notification). */
    fun updateCountdown(
        context: Context,
        nextPrayer: PrayerScheduleItem?,
        remainingMillis: Long,
        selectedLocation: SavedLocation
    ) {
        if (!isNotificationEnabled(context)) return

        val prayerName = nextPrayer?.type.displayName ?: "Prayer"
        val prayerTime = nextPrayer?.time ?: Instant.EPOCH
        val formattedTime = dateTimeFormatter.format(prayerTime.atZone(selectedLocation.timeZoneId))
        val remaining = formatDuration(remainingMillis)

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_ALARM_CHANNEL).apply {
            setSmallIcon(com.example.prayeralarm.R.drawable.ic_launcher_foreground)
            setContentTitle("Next prayer: $prayerName")
            setContentText(
                if (remainingMillis > 0L) {
                    "Time: $formattedTime\nRemaining: $remaining"
                } else {
                    "Time: $formattedTime\nRemaining: 00:00:00"
                }
            )
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(false)
            setOngoing(true)
        }

        manager.notify(NOTIFICATION_ID_NEXT_PRAYER, builder.build())
    }

    /** Cancel the persistent notification. */
    fun cancel(context: Context) {
        manager.cancel(NOTIFICATION_ID_NEXT_PRAYER)
    }

    /** Check if notification permission is granted. */
    private fun isNotificationEnabled(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            context.getSystemService(NotificationManager::class.java)
                .areNotificationsEnabledForPackage(context.packageName)
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "$hours h $minutes min $seconds s"
        else if (minutes > 0) "$minutes min $seconds s"
        else "$seconds s"
    }

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")
}