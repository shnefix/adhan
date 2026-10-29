package com.example.prayeralarm.ui.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import com.example.prayeralarm.domain.alarm.AlarmPlanResult
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import com.example.prayeralarm.ui.home.HomeUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmPlanScreen(
    homeUiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    var enabled by rememberSaveable { mutableStateOf(false) }
    var alarmCount by rememberSaveable { mutableIntStateOf(5) }
    var intervalMinutes by rememberSaveable { mutableLongStateOf(10L) }
    var timingMode by rememberSaveable { mutableStateOf(AlarmTimingMode.BOTH.name) }

    val fajr = homeUiState.prayerTimetable.firstOrNull { it.type.name == "FAJR" }
    val fajrAt = fajr?.let { LocalDateTime.of(homeUiState.currentDate, it.time) }
    val config = AlarmPlanConfig(
        alarmCount = alarmCount,
        intervalMinutes = intervalMinutes,
        timingMode = AlarmTimingMode.valueOf(timingMode),
        enabled = enabled
    )
    val result = fajrAt?.let { AlarmPlanGenerator.generate(it, config) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Fajr Alarm Plan", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Configure and preview alarms for ${homeUiState.selectedLocation.name}.",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable alarm plan")
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }

        OutlinedTextField(
            value = alarmCount.toString(),
            onValueChange = { it.toIntOrNull()?.let { value -> alarmCount = value } },
            label = { Text("Alarm count") },
            supportingText = { Text("Total alarms, including Fajr when reached") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = intervalMinutes.toString(),
            onValueChange = { it.toLongOrNull()?.let { value -> intervalMinutes = value } },
            label = { Text("Interval in minutes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlarmTimingMode.values().forEach { mode ->
                Button(onClick = { timingMode = mode.name }) {
                    Text(mode.name.replace('_', ' '))
                }
            }
        }

        Text("Preview", style = MaterialTheme.typography.titleMedium)
        when (result) {
            is AlarmPlanResult.Success -> {
                if (result.alarms.isEmpty()) {
                    Text("Alarm plan disabled.")
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            result.alarms.forEach { alarm ->
                                Text(
                                    "${alarm.sequence}. ${
                                        alarm.scheduledAt.format(
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                        )
                                    }"
                                )
                            }
                        }
                    }
                }
            }

            is AlarmPlanResult.Invalid -> Text(
                text = result.message,
                color = MaterialTheme.colorScheme.error
            )

            null -> Text("Prayer times are still being calculated.")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Saving and system scheduling will be enabled after alarm preferences are persisted.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
