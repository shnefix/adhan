package com.example.prayeralarm.ui.alarms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.prayeralarm.alarm.CountdownNotificationService
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import com.example.prayeralarm.domain.alarm.AlarmPlanResult
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import com.example.prayeralarm.domain.alarm.PlannedAlarm
import com.example.prayeralarm.ui.components.EmptyState
import com.example.prayeralarm.ui.components.ErrorBanner
import com.example.prayeralarm.ui.home.HomeUiState
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.GreenActive
import com.example.prayeralarm.ui.theme.GreenActiveContainer
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyCard
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavyInset
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.OnGold
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.ui.theme.TextTertiary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val MIN_COUNT = 1
private const val MAX_COUNT = 8
private const val MIN_INTERVAL = 1L
private const val MAX_INTERVAL = 15L

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

    val summary = when {
        !config.enabled -> "Turned off. Switch on to schedule ${config.alarmCount} alarms."
        result is AlarmPlanResult.Success && result.alarms.isNotEmpty() -> {
            val first = result.alarms.first().scheduledAt.format(timeFormatter)
            val last = result.alarms.last().scheduledAt.format(timeFormatter)
            if (result.alarms.size == 1) "1 alarm at $first"
            else "${result.alarms.size} alarms, $first to $last"
        }
        else -> "Alarms will fire at the scheduled times."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---- Header ----
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Fajr Alarms",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif,
                color = GoldPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = fajr?.let { "Fajr is at ${it.time.format(timeFormatter)} today" }
                    ?: "Waiting for prayer times",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        // ---- Plan card ----
        PlanCard(
            enabled = config.enabled,
            summary = summary,
            onEnabledChange = { enabled ->
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

        // ---- Configuration card ----
        CardSurface {
            StepperRow(
                title = "Alarm count",
                subtitle = "$MIN_COUNT to $MAX_COUNT alarms",
                value = config.alarmCount.toString(),
                canDecrease = config.alarmCount > MIN_COUNT,
                canIncrease = config.alarmCount < MAX_COUNT,
                onDecrease = { viewModel.setAlarmCount(config.alarmCount - 1) },
                onIncrease = { viewModel.setAlarmCount(config.alarmCount + 1) }
            )
            RowDivider()
            StepperRow(
                title = "Interval",
                subtitle = if (config.intervalMinutes == 1L) "minute apart" else "minutes apart",
                value = config.intervalMinutes.toString(),
                canDecrease = config.intervalMinutes > MIN_INTERVAL,
                canIncrease = config.intervalMinutes < MAX_INTERVAL,
                onDecrease = { viewModel.setIntervalMinutes(config.intervalMinutes - 1) },
                onIncrease = { viewModel.setIntervalMinutes(config.intervalMinutes + 1) }
            )
            RowDivider()
            Spacer(Modifier.height(14.dp))
            Text(
                text = "When should they ring?",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            TimingSegments(
                current = config.timingMode,
                onSelect = { viewModel.setTimingMode(it) }
            )
        }

        errorMessage?.let { ErrorBanner(message = it) }

        // ---- Preview ----
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontFamily = FontFamily.Serif,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        when (result) {
            is AlarmPlanResult.Success -> {
                if (result.alarms.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.NotificationsOff,
                        title = "Alarm plan is disabled",
                        description = "Turn on the alarm plan to schedule Fajr alarms."
                    )
                } else {
                    AlarmPreviewGrid(alarms = result.alarms, fajrAt = fajrAt)
                }
            }
            is AlarmPlanResult.Invalid -> ErrorBanner(message = result.message)
            null -> EmptyState(
                icon = Icons.Default.Info,
                title = "Calculating prayer times...",
                description = "The preview will appear once prayer times are available."
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------------------------

@Composable
private fun CardSurface(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NavySurface)
            .border(1.dp, NavyBorder, shape)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
    ) { content() }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NavyBorder.copy(alpha = 0.8f))
    )
}

@Composable
private fun PlanCard(
    enabled: Boolean,
    summary: String,
    onEnabledChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NavySurface)
            .border(1.dp, NavyBorder, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Enabled",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NavyDark,
                    checkedTrackColor = GoldPrimary,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = NavyCard,
                    uncheckedBorderColor = NavyBorder
                )
            )
        }
        Box(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(NavyBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Plan status",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (enabled) "Active" else "Disabled",
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) GreenActive else TextSecondary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (enabled) GreenActiveContainer else NavyCard)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    subtitle: String,
    value: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StepButton("−", "Decrease $title", canDecrease, onDecrease)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
            StepButton("+", "Increase $title", canIncrease, onIncrease)
        }
    }
}

@Composable
private fun StepButton(
    symbol: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .background(NavyCard)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Friendly label for an alarm timing mode. */
private fun AlarmTimingMode.displayName(): String = when (this) {
    AlarmTimingMode.BEFORE_FAJR -> "Before Fajr"
    AlarmTimingMode.AT_OR_AFTER_FAJR -> "At or after Fajr"
    AlarmTimingMode.BOTH -> "Before and after"
}

@Composable
private fun TimingSegments(
    current: AlarmTimingMode,
    onSelect: (AlarmTimingMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyInset)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AlarmTimingMode.values().forEach { mode ->
            val selected = mode == current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) GoldPrimary else Color.Transparent)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(mode) }
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.displayName(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) OnGold else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Planned alarm times as a compact grid of chips, four per row. */
@Composable
private fun AlarmPreviewGrid(
    alarms: List<PlannedAlarm>,
    fajrAt: LocalDateTime?
) {
    val fajrMinute = fajrAt?.truncatedTo(ChronoUnit.MINUTES)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alarms.chunked(4).forEach { rowAlarms ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowAlarms.forEach { alarm ->
                    val isFajr = fajrMinute != null &&
                        alarm.scheduledAt.truncatedTo(ChronoUnit.MINUTES) == fajrMinute
                    AlarmChip(
                        time = alarm.scheduledAt.format(timeFormatter),
                        tag = if (isFajr) "Fajr" else "#${alarm.sequence}",
                        highlighted = isFajr,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowAlarms.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AlarmChip(
    time: String,
    tag: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(if (highlighted) NavyCard else NavySurface)
            .border(1.dp, if (highlighted) GoldPrimary else NavyBorder, shape),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontFeatureSettings = "tnum"
            ),
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = tag,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = if (highlighted) GoldPrimary else TextTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}
