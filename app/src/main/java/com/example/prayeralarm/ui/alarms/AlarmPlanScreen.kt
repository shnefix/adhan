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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prayeralarm.domain.alarm.AlarmPlanGenerator
import com.example.prayeralarm.domain.alarm.AlarmPlanResult
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import com.example.prayeralarm.ui.home.HomeUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmPlanScreen(
    homeUiState: HomeUiState,
    viewModel: AlarmPlanViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val fajr = homeUiState.prayerTimetable.firstOrNull { it.type.name == "FAJR" }
    val fajrAt = fajr?.let { LocalDateTime.of(homeUiState.currentDate, it.time) }
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
            Switch(checked = config.enabled, onCheckedChange = viewModel::setEnabled)
        }

        OutlinedTextField(
            value = config.alarmCount.toString(),
            onValueChange = { it.toIntOrNull()?.let(viewModel::setAlarmCount) },
            label = { Text("Alarm count") },
            supportingText = { Text("Total alarms, including Fajr when reached") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = config.intervalMinutes.toString(),
            onValueChange = { it.toLongOrNull()?.let(viewModel::setIntervalMinutes) },
            label = { Text("Interval in minutes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlarmTimingMode.values().forEach { mode ->
                Button(onClick = { viewModel.setTimingMode(mode) }) {
                    Text(mode.name.replace('_', ' '))
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
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
            text = "Alarm preferences are persisted locally. System scheduling is subject to Android permissions.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
