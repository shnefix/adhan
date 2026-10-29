package com.example.prayeralarm.ui.locations.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.ui.theme.GoldPrimary
import com.example.prayeralarm.ui.theme.NavyBorder
import com.example.prayeralarm.ui.theme.NavyDark
import com.example.prayeralarm.ui.theme.NavySurface
import com.example.prayeralarm.ui.theme.TextPrimary
import com.example.prayeralarm.ui.theme.TextSecondary
import com.example.prayeralarm.util.ValidationResult

@Composable
fun AddEditLocationDialog(
    initialLocation: SavedLocation?,
    onDismiss: () -> Unit,
    onSave: (name: String, latitude: String, longitude: String, timezoneId: String) -> ValidationResult
) {
    var name by remember { mutableStateOf(initialLocation?.name ?: "") }
    var latitude by remember { mutableStateOf(initialLocation?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initialLocation?.longitude?.toString() ?: "") }
    var timezoneId by remember { mutableStateOf(initialLocation?.timeZoneId ?: "Africa/Tunis") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }
    var timezoneError by remember { mutableStateOf<String?>(null) }

    val isEditing = initialLocation != null
    val title = if (isEditing) "Edit Location" else "Add New Location"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp)),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Enter coordinates in decimal degrees and standard IANA timezone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Location Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("City / Location Name") },
                    placeholder = { Text("e.g. Sfax") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Latitude Field
                OutlinedTextField(
                    value = latitude,
                    onValueChange = {
                        latitude = it
                        latitudeError = null
                    },
                    label = { Text("Latitude (-90.0 to 90.0)") },
                    placeholder = { Text("e.g. 34.7406") },
                    isError = latitudeError != null,
                    supportingText = {
                        if (latitudeError != null) Text(latitudeError!!, color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Longitude Field
                OutlinedTextField(
                    value = longitude,
                    onValueChange = {
                        longitude = it
                        longitudeError = null
                    },
                    label = { Text("Longitude (-180.0 to 180.0)") },
                    placeholder = { Text("e.g. 10.7603") },
                    isError = longitudeError != null,
                    supportingText = {
                        if (longitudeError != null) Text(longitudeError!!, color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timezone ID Field
                OutlinedTextField(
                    value = timezoneId,
                    onValueChange = {
                        timezoneId = it
                        timezoneError = null
                    },
                    label = { Text("IANA Timezone ID") },
                    placeholder = { Text("e.g. Africa/Tunis") },
                    isError = timezoneError != null,
                    supportingText = {
                        if (timezoneError != null) Text(timezoneError!!, color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = onSave(name, latitude, longitude, timezoneId)
                    if (!result.isValid) {
                        nameError = result.nameError
                        latitudeError = result.latitudeError
                        longitudeError = result.longitudeError
                        timezoneError = result.timezoneError
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = NavyDark
                )
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Add Location",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    locationName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, NavyBorder, RoundedCornerShape(20.dp)),
        title = {
            Text(
                text = "Delete Location?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"$locationName\" from your saved locations? This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = TextPrimary
                )
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = NavyBorder,
    focusedLabelColor = GoldPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = GoldPrimary,
    errorBorderColor = MaterialTheme.colorScheme.error
)

