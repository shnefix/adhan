package com.example.prayeralarm.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayeralarm.ui.home.PrayerScheduleItem
import com.example.prayeralarm.ui.theme.GoldContainer
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyCard
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.OnGoldContainer
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val TickColor = androidx.compose.ui.graphics.Color(0xFF3A4872)

@Composable
fun NextPrayerCard(
    nextPrayer: PrayerScheduleItem?,
    remainingHours: Long,
    remainingMinutes: Long,
    remainingSeconds: Long,
    isSampleData: Boolean,
    modifier: Modifier = Modifier,
    progress: Float = 0f
) {
    val shape = RoundedCornerShape(28.dp)
    val countdown = String.format(
        "%02d:%02d:%02d",
        remainingHours.coerceAtLeast(0),
        remainingMinutes.coerceAtLeast(0),
        remainingSeconds.coerceAtLeast(0)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NavySurface)
            .border(1.dp, NavyBorder, shape)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSampleData) {
            Text(
                text = "SAMPLE",
                style = MaterialTheme.typography.labelSmall,
                color = OnGoldContainer,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GoldContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(modifier = Modifier.size(232.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val c = center
                val tickRadius = 110.dp.toPx()
                for (i in 0 until 60) {
                    val angle = Math.toRadians(i * 6.0)
                    drawCircle(
                        color = TickColor,
                        radius = 1.6.dp.toPx(),
                        center = Offset(
                            c.x + (cos(angle) * tickRadius).toFloat(),
                            c.y + (sin(angle) * tickRadius).toFloat()
                        )
                    )
                }
                val strokeWidth = 9.dp.toPx()
                val inset = 22.dp.toPx()
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(
                    color = NavyCard,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = GoldPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NEXT PRAYER",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = nextPrayer?.type?.displayName ?: "Upcoming",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
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
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 34.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = nextPrayer?.let { "until adhan at ${it.time.format(timeFormatter)}" } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
