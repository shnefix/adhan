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
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.OnGoldContainer
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.ui.theme.TextTertiary
import java.time.Duration
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun NextPrayerCard(
    nextPrayer: PrayerScheduleItem?,
    remainingHours: Long,
    remainingMinutes: Long,
    remainingSeconds: Long,
    isSampleData: Boolean,
    timetable: List<PrayerScheduleItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val remainingTotal = remainingHours * 3600 + remainingMinutes * 60 + remainingSeconds
    val progress = elapsedFraction(nextPrayer, timetable, remainingTotal)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NavySurface)
            .border(1.dp, NavyBorder, shape)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outerR = size.minDimension / 2f
                val c = center

                // Tick marks around the dial
                for (i in 0 until 60) {
                    val angle = Math.toRadians(i * 6.0 - 90.0)
                    val major = i % 5 == 0
                    val len = if (major) 8.dp.toPx() else 5.dp.toPx()
                    val dx = cos(angle).toFloat()
                    val dy = sin(angle).toFloat()
                    drawLine(
                        color = if (major) TextTertiary.copy(alpha = 0.7f) else NavyBorder,
                        start = Offset(c.x + dx * (outerR - len), c.y + dy * (outerR - len)),
                        end = Offset(c.x + dx * outerR, c.y + dy * outerR),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Track + progress arc
                val strokeW = 8.dp.toPx()
                val ringR = outerR - 22.dp.toPx()
                drawCircle(color = NavyBorder, radius = ringR, style = Stroke(strokeW))
                drawArc(
                    color = GoldPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(c.x - ringR, c.y - ringR),
                    size = Size(ringR * 2, ringR * 2),
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NEXT PRAYER",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nextPrayer?.type?.displayName ?: "Upcoming",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nextPrayer?.type?.arabicName ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = GoldPrimary
                    )
                }
                Text(
                    text = String.format(
                        "%02d:%02d:%02d",
                        remainingHours.coerceAtLeast(0),
                        remainingMinutes.coerceAtLeast(0),
                        remainingSeconds.coerceAtLeast(0)
                    ),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                if (nextPrayer != null) {
                    Text(
                        text = nextPrayer.time.format(timeFormatter),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (isSampleData) {
                    Text(
                        text = "SAMPLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnGoldContainer,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldContainer)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/** Fraction of the gap between the previous prayer and the next one that has already passed. */
private fun elapsedFraction(
    next: PrayerScheduleItem?,
    timetable: List<PrayerScheduleItem>,
    remainingSeconds: Long
): Float {
    if (next == null || timetable.isEmpty()) return 0f
    val idx = timetable.indexOfFirst { it.type == next.type }
    val prev = if (idx > 0) timetable[idx - 1] else timetable.last()
    var span = Duration.between(prev.time, next.time).seconds
    if (span <= 0) span += 86_400
    return (1f - remainingSeconds.toFloat() / span).coerceIn(0f, 1f)
}