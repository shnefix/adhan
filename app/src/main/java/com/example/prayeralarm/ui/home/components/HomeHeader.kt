package com.example.prayeralarm.ui.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayeralarm.ui.theme.GoldPrimary

/** Hijri date on a single line. */
@Composable
fun HomeHeader(
    hijriDate: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = hijriDate,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
        fontFamily = FontFamily.Serif,
        color = GoldPrimary,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}
