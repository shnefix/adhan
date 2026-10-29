package com.example.prayeralarm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.prayeralarm.domain.model.PrayerType
import com.example.prayeralarm.ui.home.components.HomeHeader
import com.example.prayeralarm.ui.home.components.NextPrayerCard
import com.example.prayeralarm.ui.home.components.PrayerTimetable
import com.example.prayeralarm.ui.theme.PrayerAlarmTheme
import java.time.Duration

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeHeader(
                hijriDate = uiState.hijriDate,
                date = uiState.currentDate
            )

            NextPrayerCard(
                nextPrayer = uiState.nextPrayer,
                remainingHours = uiState.remainingHours,
                remainingMinutes = uiState.remainingMinutes,
                remainingSeconds = uiState.remainingSeconds,
                progress = computeProgress(uiState),
                isSampleData = uiState.isSampleData
            )

            PrayerTimetable(
                timetable = uiState.prayerTimetable,
                onToggleNotification = onToggleNotification
            )
        }
    }
}

/**
 * How far we are between the previous prayer and the next one (0f..1f).
 * Before Fajr the previous prayer is yesterday's Isha, so the gap wraps over midnight.
 */
private fun computeProgress(state: HomeUiState): Float {
    val next = state.nextPrayer ?: return 0f
    val sorted = state.prayerTimetable.sortedBy { it.time }
    if (sorted.isEmpty()) return 0f

    val index = sorted.indexOfFirst { it.type == next.type }
    val gapSeconds = if (index > 0) {
        Duration.between(sorted[index - 1].time, next.time).seconds
    } else {
        Duration.between(sorted.last().time, next.time).seconds + 86_400L
    }
    if (gapSeconds <= 0L) return 0f

    val remaining = state.remainingHours * 3600 + state.remainingMinutes * 60 + state.remainingSeconds
    return (1f - remaining.toFloat() / gapSeconds.toFloat()).coerceIn(0f, 1f)
}

@Preview(name = "Home Screen", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    PrayerAlarmTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeViewModel.createPreviewState(),
            onToggleNotification = {}
        )
    }
}
