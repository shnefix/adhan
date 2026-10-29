package com.example.prayeralarm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.prayeralarm.domain.model.SavedLocation

/**
 * Room SQLite Entity representing a persisted location.
 *
 * Mapped to the table "saved_locations".
 *
 * @property id Unique auto-generated primary key in SQLite.
 * @property name Human-readable city/location name (e.g., "Tunis", "Sfax").
 * @property latitude Geographic latitude in decimal degrees (-90.0 to +90.0).
 * @property longitude Geographic longitude in decimal degrees (-180.0 to +180.0).
 * @property timeZoneId Canonical IANA timezone identifier (e.g., "Africa/Tunis").
 */
@Entity(tableName = "saved_locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "timezone_id")
    val timeZoneId: String
)

/**
 * Conversion extensions between database entities and domain models.
 */
fun LocationEntity.toDomain(isSelected: Boolean = false): SavedLocation {
    return SavedLocation(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId,
        isDefault = isSelected
    )
}

fun SavedLocation.toEntity(): LocationEntity {
    return LocationEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId
    )
}

