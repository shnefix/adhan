package com.example.prayeralarm.util

import java.time.ZoneId

/**
 * Result of validating user-input location parameters.
 */
data class ValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val latitudeError: String? = null,
    val longitudeError: String? = null,
    val timezoneError: String? = null
)

/**
 * Validates coordinate inputs, name, and IANA timezone ID.
 */
object LocationValidator {

    private const val MIN_LATITUDE = -90.0
    private const val MAX_LATITUDE = 90.0
    private const val MIN_LONGITUDE = -180.0
    private const val MAX_LONGITUDE = 180.0

    /**
     * Comprehensive validation of all location fields.
     */
    fun validate(
        name: String,
        latitudeStr: String,
        longitudeStr: String,
        timezoneId: String
    ): ValidationResult {
        val nameError = validateName(name)
        val latError = validateLatitude(latitudeStr)
        val lonError = validateLongitude(longitudeStr)
        val tzError = validateTimezoneId(timezoneId)

        val isValid = nameError == null && latError == null && lonError == null && tzError == null
        return ValidationResult(
            isValid = isValid,
            nameError = nameError,
            latitudeError = latError,
            longitudeError = lonError,
            timezoneError = tzError
        )
    }

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Location name cannot be empty"
            trimmed.length < 2 -> "Name must be at least 2 characters"
            trimmed.length > 50 -> "Name must not exceed 50 characters"
            else -> null
        }
    }

    fun validateLatitude(latitudeStr: String): String? {
        val trimmed = latitudeStr.trim()
        if (trimmed.isEmpty()) return "Latitude is required"
        val lat = trimmed.toDoubleOrNull() ?: return "Latitude must be a valid decimal number"
        return if (lat in MIN_LATITUDE..MAX_LATITUDE) {
            null
        } else {
            "Latitude must be between -90.0 and +90.0"
        }
    }

    fun validateLongitude(longitudeStr: String): String? {
        val trimmed = longitudeStr.trim()
        if (trimmed.isEmpty()) return "Longitude is required"
        val lon = trimmed.toDoubleOrNull() ?: return "Longitude must be a valid decimal number"
        return if (lon in MIN_LONGITUDE..MAX_LONGITUDE) {
            null
        } else {
            "Longitude must be between -180.0 and +180.0"
        }
    }

    fun validateTimezoneId(timezoneId: String): String? {
        val trimmed = timezoneId.trim()
        if (trimmed.isEmpty()) return "Timezone ID is required"
        return try {
            val available = ZoneId.getAvailableZoneIds()
            if (available.contains(trimmed)) {
                null
            } else {
                // Also verify if ZoneId.of can resolve it
                ZoneId.of(trimmed)
                null
            }
        } catch (e: Exception) {
            "Invalid IANA timezone ID (e.g., Africa/Tunis, Europe/London)"
        }
    }
}

