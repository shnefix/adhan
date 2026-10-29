package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.entity.LocationEntity

/**
 * The initial location catalog shown on a fresh installation.
 *
 * These are only defaults: users can add, edit, and select any other city later.
 * All three Tunisian cities use the same IANA zone because they are in the same
 * country. Their different coordinates still produce slightly different solar
 * times.
 */
object DefaultLocations {

    /**
     * Order matters for first-run selection: Mahdia is the predictable default.
     */
    val all: List<LocationEntity> = listOf(
        LocationEntity(
            name = "Mahdia",
            latitude = 35.5047,
            longitude = 11.0622,
            timeZoneId = "Africa/Tunis"
        ),
        LocationEntity(
            name = "Ariana",
            latitude = 36.8625,
            longitude = 10.1956,
            timeZoneId = "Africa/Tunis"
        ),
        LocationEntity(
            name = "Kelibia",
            latitude = 36.8476,
            longitude = 11.0939,
            timeZoneId = "Africa/Tunis"
        )
    )

    /** The location selected when a fresh database is seeded. */
    val primary: LocationEntity = all.first()

    /**
     * Identity check used by migration/seeding and duplicate prevention.
     * A user is allowed to create another location with the same name as long as
     * its coordinates or time zone differ.
     */
    fun matches(entity: LocationEntity): Boolean {
        return all.any { default ->
            default.name == entity.name &&
                default.latitude == entity.latitude &&
                default.longitude == entity.longitude &&
                default.timeZoneId == entity.timeZoneId
        }
    }
}
