package com.example.prayeralarm.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class DefaultLocationsTest {

    @Test
    fun defaults_containPreferredLocationsWithoutDuplicates() {
        val defaults = DefaultLocations.all

        assertEquals(listOf("Mahdia", "Kelibia", "Ariana"), defaults.map { it.name })
        assertEquals(defaults.size, defaults.map { it.name }.toSet().size)
        assertTrue(defaults.all { it.timeZoneId == "Africa/Tunis" })
        assertTrue(defaults.all { ZoneId.getAvailableZoneIds().contains(it.timeZoneId) })
    }

    @Test
    fun defaults_haveValidCoordinates() {
        assertTrue(DefaultLocations.all.all { it.latitude in -90.0..90.0 })
        assertTrue(DefaultLocations.all.all { it.longitude in -180.0..180.0 })
    }
}
