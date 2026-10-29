package com.example.prayeralarm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

/**
 * The whole screen fits without scrolling: the header and the next-prayer card take
 * their natural height and the five prayer rows share all the remaining space.
 */
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
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HomeHeader(hijriDate = uiState.hijriDate)

            NextPrayerCard(
                nextPrayer = uiState.nextPrayer,
                remainingHours = uiState.remainingHours,
                remainingMinutes = uiState.remainingMinutes,
                remainingSeconds = uiState.remainingSeconds,
                isSampleData = uiState.isSampleData
            )

            PrayerTimetable(
                timetable = uiState.prayerTimetable,
                onToggleNotification = onToggleNotification,
                modifier = Modifier.weight(1f)
            )
        }
    }
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
