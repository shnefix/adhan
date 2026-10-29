package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.entity.LocationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class DefaultLocationsTest {

    @Test
    fun defaults_containExactlyTheThreeRequestedCitiesInOrder() {
        val defaults = DefaultLocations.all

        assertEquals(listOf("Mahdia", "Ariana", "Kelibia"), defaults.map { it.name })
        assertEquals(defaults.size, defaults.map { it.name }.toSet().size)
        assertTrue(defaults.all { it.timeZoneId == "Africa/Tunis" })
        assertTrue(defaults.all { ZoneId.getAvailableZoneIds().contains(it.timeZoneId) })
    }

    @Test
    fun defaults_haveTheExactRequestedCoordinates() {
        val expected = mapOf(
            "Mahdia" to (35.5047 to 11.0622),
            "Ariana" to (36.8625 to 10.1956),
            "Kelibia" to (36.8476 to 11.0939)
        )

        DefaultLocations.all.forEach { location ->
            val (latitude, longitude) = expected.getValue(location.name)
            assertEquals("$location.name latitude", latitude, location.latitude, 0.0)
            assertEquals("$location.name longitude", longitude, location.longitude, 0.0)
        }
    }

    @Test
    fun defaults_haveValidCoordinates() {
        assertTrue(DefaultLocations.all.all { it.latitude in -90.0..90.0 })
        assertTrue(DefaultLocations.all.all { it.longitude in -180.0..180.0 })
    }

    @Test
    fun primary_isMahdia() {
        assertSame(DefaultLocations.all.first(), DefaultLocations.primary)
        assertEquals("Mahdia", DefaultLocations.primary.name)
    }

    @Test
    fun matches_requiresNameCoordinatesAndZone() {
        assertTrue(DefaultLocations.matches(DefaultLocations.primary))
        assertTrue(DefaultLocations.matches(DefaultLocations.all[1]))
        assertTrue(DefaultLocations.matches(DefaultLocations.all[2]))

        assertFalse(
            "Same name but different coordinates must not match",
            DefaultLocations.matches(
                LocationEntity(
                    name = "Mahdia",
                    latitude = 35.0,
                    longitude = 11.0622,
                    timeZoneId = "Africa/Tunis"
                )
            )
        )
        assertFalse(
            "Same coordinates but different zone must not match",
            DefaultLocations.matches(
                LocationEntity(
                    name = "Mahdia",
                    latitude = 35.5047,
                    longitude = 11.0622,
                    timeZoneId = "Europe/Paris"
                )
            )
        )
    }
}
