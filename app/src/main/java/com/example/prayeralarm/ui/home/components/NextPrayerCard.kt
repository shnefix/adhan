package com.example.prayeralarm.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayeralarm.ui.home.PrayerScheduleItem
import com.example.prayeralarm.ui.theme.GoldContainer
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.OnGoldContainer
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun NextPrayerCard(
    nextPrayer: PrayerScheduleItem?,
    remainingHours: Long,
    remainingMinutes: Long,
    remainingSeconds: Long,
    isSampleData: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NavySurface)
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), shape)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NEXT PRAYER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSampleData) {
                        Text(
                            text = "SAMPLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnGoldContainer,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldContainer)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = nextPrayer?.type?.displayName ?: "Upcoming",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = nextPrayer?.type?.arabicName ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            if (nextPrayer != null) {
                Text(
                    text = nextPrayer.time.format(timeFormatter),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(NavyDark)
                        .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            CountdownUnit(value = remainingHours, label = "HOURS")
            CountdownColon()
            CountdownUnit(value = remainingMinutes, label = "MINS")
            CountdownColon()
            CountdownUnit(value = remainingSeconds, label = "SECS")
        }
    }
}

@Composable
private fun CountdownUnit(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d", value.coerceAtLeast(0)),
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontFeatureSettings = "tnum"
            ),
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CountdownColon() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp, lineHeight = 48.sp),
        color = GoldPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
