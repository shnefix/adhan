package com.example.prayeralarm.ui.alarms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import com.example.prayeralarm.domain.alarm.AlarmPlanResult
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import com.example.prayeralarm.alarm.CountdownNotificationService
import com.example.prayeralarm.ui.components.EmptyState
import com.example.prayeralarm.ui.components.ErrorBanner
import com.example.prayeralarm.ui.components.PrayerDivider
import com.example.prayeralarm.ui.components.PrayerSectionHeader
import com.example.prayeralarm.ui.components.PrayerToggleRow
import com.example.prayeralarm.ui.components.SettingsGroup
import com.example.prayeralarm.ui.components.SettingsRow
import com.example.prayeralarm.ui.components.StatusChip
import com.example.prayeralarm.ui.home.HomeUiState
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmPlanScreen(
    homeUiState: HomeUiState,
    viewModel: AlarmPlanViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(config.enabled) {
        if (config.enabled) {
            CountdownNotificationService.start(context)
        } else {
            CountdownNotificationService.stop(context)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (config.enabled) {
            CountdownNotificationService.start(context)
        }
    }

    val fajr = homeUiState.prayerTimetable.firstOrNull { it.type.name == "FAJR" }
    val fajrAt = fajr?.let { LocalDateTime.of(homeUiState.currentDate, it.time) }
    val result = fajrAt?.let { AlarmPlanGenerator.generate(it, config) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---- Screen header ----
        PrayerSectionHeader(
            title = "Fajr Alarms",
        )

        // ---- Section: Alarm status (primary control) ----
        PrayerSectionHeader(
            title = "Alarm plan",
        )
        SettingsGroup {
            PrayerToggleRow(
                title = "Enabled",
                description = if (config.enabled) "Alarms are active and will fire at the scheduled times." else "Alarms are disabled. Turn on to schedule Fajr alarms.",
                checked = config.enabled,
                onCheckedChange = { enabled ->
                    viewModel.setEnabled(enabled)
                    if (enabled &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            SettingsRow(
                    icon = Icons.Default.Info,
                title = "Plan status",
                description = if (config.enabled) "The alarm plan is active." else "The alarm plan is disabled. No alarms will fire.",
                trailing = {
                    StatusChip(
                        active = config.enabled,
                        label = if (config.enabled) "Active" else "Disabled"
                    )
                }
            )
        }

        // ---- Section: Configuration ----
        PrayerSectionHeader(
            title = "Configuration",
        )
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.AccessAlarm,
                title = "Alarm count",
                trailing = {
                    OutlinedTextField(
                        value = config.alarmCount.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { viewModel.setAlarmCount(it) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(96.dp)
                    )
                }
            )
            SettingsRow(
                icon = Icons.Default.Timer,
                title = "Interval",
                trailing = {
                    OutlinedTextField(
                        value = config.intervalMinutes.toString(),
                        onValueChange = { value ->
                            value.toLongOrNull()?.let { viewModel.setIntervalMinutes(it) }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(96.dp)
                    )
                }
            )
            TimingModeSelector(
                current = config.timingMode,
                onSelect = { viewModel.setTimingMode(it) }
            )
        }

        // Error banner (shown once, not repeated per control)
        errorMessage?.let {
            ErrorBanner(message = it)
        }

        // ---- Section: Preview ----
        PrayerSectionHeader(
            title = "Preview",
        )
        when (result) {
            is AlarmPlanResult.Success -> {
                if (result.alarms.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.NotificationsOff,
                        title = "Alarm plan is disabled",
                        description = "Turn on the alarm plan to schedule Fajr alarms."
                    )
                } else {
                    AlarmPreviewList(alarms = result.alarms)
                }
            }
            is AlarmPlanResult.Invalid -> ErrorBanner(message = result.message)
            null -> EmptyState(
                    icon = Icons.Default.Info,
                title = "Calculating prayer times…",
                description = "The preview will appear once prayer times are available."
            )
        }

    }
}

/** Friendly label for an alarm timing mode. */
private fun AlarmTimingMode.displayName(): String = when (this) {
    AlarmTimingMode.BEFORE_FAJR -> "Before Fajr"
    AlarmTimingMode.AT_OR_AFTER_FAJR -> "At or after Fajr"
    AlarmTimingMode.BOTH -> "Before and after"
}

/** Selector for the alarm timing mode relative to Fajr. */
@Composable
private fun TimingModeSelector(
    current: AlarmTimingMode,
    onSelect: (AlarmTimingMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Timing",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Choose when alarms fire relative to Fajr.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlarmTimingMode.values().forEach { mode ->
                val isSelected = current == mode
                Button(
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else NavySurface,
                        contentColor = if (isSelected) NavyDark else TextPrimary,
                        disabledContainerColor = NavySurface
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = mode.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    PrayerDivider(modifier = Modifier.padding(start = 16.dp))
}

/** Displays the list of planned alarm times. */
@Composable
private fun AlarmPreviewList(
    alarms: List<com.example.prayeralarm.domain.alarm.PlannedAlarm>
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        alarms.forEach { alarm ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessAlarm,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alarm.scheduledAt.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "#${alarm.sequence}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
            if (alarm.sequence != alarms.size) {
                PrayerDivider(modifier = Modifier.padding(start = 40.dp))
            }
        }
    }
}
