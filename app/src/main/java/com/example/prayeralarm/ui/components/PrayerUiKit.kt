package com.example.prayeralarm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyCard
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.ui.theme.TextTertiary

/** Minimal spacing tokens used across redesigned screens. */
object PrayerSpacing {
    val screenPadding = 16.dp
    val sectionSpacing = 20.dp
    var groupPadding = 16.dp
    var itemSpacing = 12.dp
    val iconSize = 20.dp
    val iconContainerSize = 40.dp
    val chipHeight = 28.dp
    val dividerHeight = 1.dp
}

/** Colors reused across redesigned UI components. */
object PrayerUiColors {
    val surface = NavyCard
    val border = Color(0xFF3A506B)
    val active = Color(0xFF4CAF50)
    val disabled = Color(0xFF78909C)
    val containerActive = Color(0xFF1B3820)
}

/**
 * A section header: a prominent title with an optional supporting description.
 */
@Composable
fun PrayerSectionHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

/**
 * A subtle divider between sections or rows.
 */
@Composable
fun PrayerDivider(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .height(PrayerSpacing.dividerHeight)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.outline)
    )
}

/**
 * A grouped surface that wraps related settings rows with a border and rounded corners.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrayerUiColors.surface)
            .border(
                width = PrayerSpacing.dividerHeight,
                color = PrayerUiColors.border,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

/**
 * A single settings row: leading icon, title, optional description, trailing value or control.
 *
 * The row is [48.dp] tall for accessible touch targets when [onClick] is provided.
 */
@Composable
fun SettingsRow(
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    value: String? = null,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isClickable = onClick != null

    val contentModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 16.dp)

    Row(
        modifier = modifier
            .then(contentModifier)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = { onClick() })
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(PrayerSpacing.iconSize)
                    .wrapContentSize(Alignment.Center),
                tint = GoldPrimary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }

        trailing()
    }

    if (isClickable) {
        PrayerDivider(modifier = Modifier.padding(start = 16.dp))
    }
}

/**
 * A small status chip showing enabled/active or disabled state.
 */
@Composable
fun StatusChip(
    active: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val text = if (active) label else label
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PrayerSpacing.chipHeight / 2))
            .background(
                color = if (active) PrayerUiColors.containerActive else Color(0xFF232F55)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) PrayerUiColors.active else PrayerUiColors.disabled,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

/**
 * A primary toggle row: leading label + description, trailing switch.
 */
@Composable
fun PrayerToggleRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = NavyDark,
                uncheckedThumbColor = TextPrimary,
                checkedTrackColor = GoldPrimary,
                uncheckedTrackColor = PrayerUiColors.border
            )
        )
    }
    PrayerDivider(modifier = Modifier.padding(start = 16.dp))
}

/**
 * An empty state illustration: icon, title, and supporting description.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = TextTertiary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * An error banner shown inline when configuration is invalid.
 */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1F1F))
            .border(
                width = 1.dp,
                color = Color(0xFF7F1D1D),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFFB4AB),
            maxLines = 3
        )
    }
}
