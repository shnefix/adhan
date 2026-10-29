package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.entity.LocationEntity

object DefaultLocations {
    val all: List<LocationEntity> = listOf(
        LocationEntity(
            name = "Mahdia",
            latitude = 35.5047,
            longitude = 11.0622,
            timeZoneId = "Africa/Tunis"
        ),
        LocationEntity(
            name = "Kelibia",
            latitude = 36.8476,
            longitude = 11.0939,
            timeZoneId = "Africa/Tunis"
        ),
        LocationEntity(
            name = "Ariana",
            latitude = 36.8625,
            longitude = 10.1956,
            timeZoneId = "Africa/Tunis"
        )
    )
}
