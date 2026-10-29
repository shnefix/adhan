package com.example.prayeralarm.domain.model

data class SavedLocation(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val isDefault: Boolean = false
)

