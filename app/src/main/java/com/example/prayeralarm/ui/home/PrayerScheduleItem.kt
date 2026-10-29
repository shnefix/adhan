package com.example.prayeralarm.ui.home

import com.example.prayeralarm.domain.model.PrayerType
import java.time.LocalTime

/**
 * UI model representing a single prayer entry in the timetable.
 *
 * @param type The prayer enum (Fajr, Dhuhr, etc.)
 * @param time The calculated prayer time as LocalTime
 * @param isNextPrayer True if this prayer is the upcoming prayer to be highlighted
 * @param isNotificationEnabled True if notifications are active for this specific prayer
 */
data class PrayerScheduleItem(
    val type: PrayerType,
    val time: LocalTime,
    val isNextPrayer: Boolean = false,
    val isNotificationEnabled: Boolean = true
)

