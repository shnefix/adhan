package com.example.prayeralarm.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.ui.home.PrayerScheduleItem
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyCard
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.ui.theme.TextTertiary
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun PrayerTimetable(
    timetable: List<PrayerScheduleItem>,
    onToggleNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextIndex = timetable.indexOfFirst { it.isNextPrayer }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timetable.forEachIndexed { index, item ->
            val passed = if (nextIndex == -1) true else index < nextIndex
            PrayerTimetableRow(
                item = item,
                isPassed = passed,
                onToggleNotification = { onToggleNotification(item.type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PrayerTimetableRow(
    item: PrayerScheduleItem,
    onToggleNotification: () -> Unit,
    modifier: Modifier = Modifier,
    isPassed: Boolean = false
) {
    val isNext = item.isNextPrayer
    val shape = RoundedCornerShape(18.dp)
    val nameColor = if (isPassed && !isNext) TextSecondary else TextPrimary
    val timeColor = when {
        isNext -> TextPrimary
        isPassed -> TextTertiary
        else -> TextSecondary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(if (isNext) NavyCard else NavySurface)
            .border(1.dp, if (isNext) GoldPrimary.copy(alpha = 0.6f) else NavyBorder, shape)
            .padding(start = 18.dp, end = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.type.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = nameColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.type.arabicName,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                color = TextTertiary
            )
            if (isNext) {
                Text(
                    text = "NEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(GoldPrimary.copy(alpha = 0.16f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.time.format(timeFormatter),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = timeColor,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onToggleNotification,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (item.isNotificationEnabled) {
                        Icons.Default.Notifications
                    } else {
                        Icons.Default.NotificationsOff
                    },
                    contentDescription = "${item.type.displayName} alert " +
                        if (item.isNotificationEnabled) "on" else "off",
                    tint = if (item.isNotificationEnabled) GoldPrimary else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}