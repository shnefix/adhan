package com.example.prayeralarm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destination routes and metadata for Prayer Alarm.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Prayer Times", Icons.Default.Mosque)
    data object Locations : Screen("locations", "Locations", Icons.Default.LocationOn)
    data object Alarms : Screen("alarms", "Fajr Alarms", Icons.Default.Alarm)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

