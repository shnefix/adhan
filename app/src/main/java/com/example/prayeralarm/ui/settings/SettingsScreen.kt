package com.example.prayeralarm.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.prayeralarm.domain.calculator.PrayerCalculationMethod
import com.example.prayeralarm.ui.components.PrayerSectionHeader
import com.example.prayeralarm.ui.components.SettingsGroup
import com.example.prayeralarm.ui.components.SettingsRow
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLocationChange: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedLocation by viewModel.selectedLocation.collectAsState(initial = null)
    val calculationMethod by viewModel.calculationMethod.collectAsState(initial = PrayerCalculationMethod.DEFAULT)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val exactAlarmAvailable = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        alarmManager.canScheduleExactAlarms()
    var notificationsGranted by remember {
        mutableStateOf(
            NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED)
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        notificationsGranted =
            NotificationManagerCompat.from(context).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED)
    }

    val openExactAlarmPermission = {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            )
        )
    }

    val openNotificationSettings = {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PrayerSectionHeader(
            title = "Settings",
        )

        // ---- Location ----
        PrayerSectionHeader(
            title = "Location",
        )
        SettingsGroup {
            selectedLocation?.let { location ->
                SettingsRow(
                    icon = Icons.Default.LocationOn,
                    title = "Active location",
                    description = "Timezone: ${location.timeZoneId}",
                    value = location.name,
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = TextSecondary
                        )
                    },
                    onClick = onLocationChange
                )
            } ?: run {
                SettingsRow(
                    icon = Icons.Default.LocationOff,
                    title = "No active location",
                    description = "Select a location",
                    trailing = {
                        Button(
                            onClick = onLocationChange,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text("Select location")
                        }
                    }
                )
            }
        }

        // ---- Prayer calculation ----
        PrayerSectionHeader(
            title = "Prayer Calculation",
            description = "Conventions and astronomical parameters used to compute prayer timetables."
        )
        SettingsGroup {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Default.WbSunny,
                    title = "Method",
                    description = calculationMethod.summary,
                    value = calculationMethod.displayName,
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select calculation method",
                            modifier = Modifier.size(24.dp),
                            tint = GoldPrimary
                        )
                    },
                    onClick = { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(NavySurface)
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                ) {
                    PrayerCalculationMethod.values().forEach { method ->
                        val isSelected = method == calculationMethod
                        DropdownMenuItem(
                            text = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = method.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) GoldPrimary else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = method.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                viewModel.setCalculationMethod(method)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // ---- Alarms ----
        PrayerSectionHeader(
            title = "Alarms",
        )
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Alarm,
                title = "Exact alarms",
                value = if (exactAlarmAvailable) "Available" else "Permission required"
            )
            if (!exactAlarmAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Button(
                    onClick = openExactAlarmPermission,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("Open exact-alarm permission")
                }
            }
        }

        // ---- Notifications ----
        PrayerSectionHeader(
            title = "Notifications",
        )
        SettingsGroup {
            SettingsRow(
                icon = if (notificationsGranted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                title = "Notifications",
                value = if (notificationsGranted) "Allowed" else "Permission required"
            )
            if (!notificationsGranted) {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Button(
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text("Allow notifications")
                    }
                }
                Button(
                    onClick = openNotificationSettings,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("Open notification settings")
                }
            }
        }

    }
}
