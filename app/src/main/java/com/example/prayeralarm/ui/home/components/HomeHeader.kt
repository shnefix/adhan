package com.example.prayeralarm.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

@Composable
fun HomeHeader(
    hijriDate: String,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = hijriDate,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 25.sp),
            fontFamily = FontFamily.Serif,
            color = GoldPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = date.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
