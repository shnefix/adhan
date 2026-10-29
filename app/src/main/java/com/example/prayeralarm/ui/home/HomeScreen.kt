package com.example.prayeralarm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onLocationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            HomeHeader(
                locationName = uiState.selectedLocation.name,
                currentDate = uiState.currentDate,
                hijriDate = uiState.hijriDate,
                onLocationClick = onLocationClick,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(4.dp))

            NextPrayerCard(
                nextPrayer = uiState.nextPrayer,
                remainingHours = uiState.remainingHours,
                remainingMinutes = uiState.remainingMinutes,
                remainingSeconds = uiState.remainingSeconds,
                isSampleData = uiState.isSampleData
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrayerTimetable(
                timetable = uiState.prayerTimetable,
                onToggleNotification = onToggleNotification
            )
        }
    }
}

@Preview(name = "Home Screen - Dark Islamic Theme", showBackground = true)
@Composable
fun HomeScreenPreview() {
    PrayerAlarmTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeViewModel.createPreviewState(),
            onLocationClick = {},
            onSettingsClick = {},
            onToggleNotification = {}
        )
    }
}

@Preview(name = "Next Prayer Card Only", showBackground = true)
@Composable
fun NextPrayerCardPreview() {
    val sampleState = HomeViewModel.createPreviewState()
    PrayerAlarmTheme(darkTheme = true) {
        NextPrayerCard(
            nextPrayer = sampleState.nextPrayer,
            remainingHours = 2,
            remainingMinutes = 14,
            remainingSeconds = 30,
            isSampleData = true
        )
    }
}

