package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.entity.LocationEntity
import com.example.prayeralarm.data.preferences.FakeLocationSelectionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationSeederTest {

    private lateinit var dao: FakeLocationDao
    private lateinit var selectionStore: FakeLocationSelectionStore
    private lateinit var seeder: LocationSeeder

    @Before
    fun setUp() {
        dao = FakeLocationDao()
        selectionStore = FakeLocationSelectionStore()
        seeder = LocationSeeder(dao, selectionStore)
    }

    @Test
    fun emptyDatabase_seedsThreeDefaultsOnceAndSelectsMahdia() = runTest {
        val first = seeder.seedIfNeeded()

        assertTrue(first.seeded)
        assertNotNull(first.selectedLocationId)
        assertFalse(first.repairedSelection)

        val locations = dao.getAllLocations().first()
        assertEquals(listOf("Ariana", "Kelibia", "Mahdia"), locations.map { it.name })
        assertEquals(3, locations.size)

        val selected = dao.getLocationById(first.selectedLocationId!!)
        assertEquals("Mahdia", selected?.name)
        assertEquals(35.5047, selected?.latitude ?: 0.0, 0.0)
        assertEquals(11.0622, selected?.longitude ?: 0.0, 0.0)
        assertEquals(selectionStore.currentId, first.selectedLocationId)

        val second = seeder.seedIfNeeded()
        assertFalse(second.seeded)
        assertEquals(first.selectedLocationId, second.selectedLocationId)
        assertEquals(3, dao.getLocationCount())
    }

    @Test
    fun existingLocationsAndValidSelection_arePreserved() = runTest {
        val sfaxId = dao.insertLocation(
            LocationEntity(
                name = "Sfax",
                latitude = 34.7406,
                longitude = 10.7603,
                timeZoneId = "Africa/Tunis"
            )
        )
        dao.insertLocation(
            LocationEntity(
                name = "Tunis",
                latitude = 36.8065,
                longitude = 10.1815,
                timeZoneId = "Africa/Tunis"
            )
        )
        selectionStore.setSelectedLocationId(sfaxId)

        val result = seeder.seedIfNeeded()

        assertFalse(result.seeded)
        assertFalse(result.repairedSelection)
        assertEquals(sfaxId, result.selectedLocationId)
        assertEquals(sfaxId, selectionStore.currentId)
        assertEquals(2, dao.getLocationCount())
        assertEquals("Sfax", dao.getLocationById(sfaxId)?.name)
    }

    @Test
    fun upgradeWithExistingDefaultLikeRow_doesNotDuplicateOrOverwriteIt() = runTest {
        // Simulates an upgraded install that already had a row with the same name
        // but user-edited coordinates. The seeder must not touch it.
        val editedMahdiaId = dao.insertLocation(
            LocationEntity(
                name = "Mahdia",
                latitude = 35.0,
                longitude = 11.0,
                timeZoneId = "Africa/Tunis"
            )
        )
        selectionStore.setSelectedLocationId(editedMahdiaId)

        val result = seeder.seedIfNeeded()

        assertFalse(result.seeded)
        assertEquals(1, dao.getLocationCount())
        val preserved = dao.getLocationById(editedMahdiaId)
        assertEquals(35.0, preserved?.latitude ?: 0.0, 0.0)
        assertEquals(11.0, preserved?.longitude ?: 0.0, 0.0)
        assertEquals(editedMahdiaId, result.selectedLocationId)
    }

    @Test
    fun existingLocationsWithUnsetSelection_selectsFirstAlphabetically() = runTest {
        val tunisId = dao.insertLocation(
            LocationEntity(
                name = "Tunis",
                latitude = 36.8065,
                longitude = 10.1815,
                timeZoneId = "Africa/Tunis"
            )
        )
        val arianaId = dao.insertLocation(
            LocationEntity(
                name = "Ariana",
                latitude = 36.8625,
                longitude = 10.1956,
                timeZoneId = "Africa/Tunis"
            )
        )

        val result = seeder.seedIfNeeded()

        assertFalse(result.seeded)
        assertTrue(result.repairedSelection)
        assertEquals(arianaId, result.selectedLocationId)
        assertEquals(arianaId, selectionStore.currentId)
        assertEquals(2, dao.getLocationCount())
        assertNotNull(dao.getLocationById(tunisId))
    }

    @Test
    fun danglingSelection_isRepairedWithoutDeletingRows() = runTest {
        val firstId = dao.insertLocation(
            LocationEntity(
                name = "Kelibia",
                latitude = 36.8476,
                longitude = 11.0939,
                timeZoneId = "Africa/Tunis"
            )
        )
        dao.insertLocation(
            LocationEntity(
                name = "Mahdia",
                latitude = 35.5047,
                longitude = 11.0622,
                timeZoneId = "Africa/Tunis"
            )
        )
        selectionStore.setSelectedLocationId(9_999L)

        val result = seeder.seedIfNeeded()

        assertTrue(result.repairedSelection)
        // Alphabetical first row is Kelibia (the inserted id), not the dangling ID.
        assertEquals(firstId, result.selectedLocationId)
        assertEquals(firstId, selectionStore.currentId)
        assertEquals(2, dao.getLocationCount())
    }
}
