package com.example.prayeralarm.data.repository

import com.example.prayeralarm.data.local.FakeLocationDao
import com.example.prayeralarm.data.preferences.FakeLocationSelectionStore
import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests the real [LocationRepositoryImpl] against in-memory persistence fakes.
 */
class LocationRepositoryImplTest {

    private lateinit var dao: FakeLocationDao
    private lateinit var selectionStore: FakeLocationSelectionStore
    private lateinit var repository: LocationRepositoryImpl

    private val mahdia = SavedLocation(
        name = "Mahdia",
        latitude = 35.5047,
        longitude = 11.0622,
        timeZoneId = "Africa/Tunis"
    )
    private val ariana = SavedLocation(
        name = "Ariana",
        latitude = 36.8625,
        longitude = 10.1956,
        timeZoneId = "Africa/Tunis"
    )

    @Before
    fun setUp() {
        dao = FakeLocationDao()
        selectionStore = FakeLocationSelectionStore()
        repository = LocationRepositoryImpl(dao, selectionStore)
    }

    @Test
    fun addLocation_persistsAndEmitsIt() = runTest {
        val id = repository.addLocation(mahdia)

        assertTrue(id > 0)
        val all = repository.getAllLocations().first()
        assertEquals(1, all.size)
        assertEquals("Mahdia", all.single().name)
        assertFalse("Unselected locations must not be marked default", all.single().isDefault)
    }

    @Test
    fun addLocation_marksItSelectedWhenRequested() = runTest {
        val id = repository.addLocation(mahdia.copy(isDefault = true))

        val active = repository.getActiveLocation().first()
        assertEquals(id, active?.id)
        assertTrue(repository.getAllLocations().first().single().isDefault)
        assertEquals(id, selectionStore.currentId)
    }

    @Test
    fun getActiveLocation_fallsBackToAlphabeticalFirstWhenSelectionUnset() = runTest {
        repository.addLocation(mahdia)
        repository.addLocation(ariana)

        val active = repository.getActiveLocation().first()

        assertEquals("Ariana", active?.name)
        assertTrue(active?.isDefault == true)
    }

    @Test
    fun getActiveLocation_fallsBackWhenStoredSelectionIsDangling() = runTest {
        repository.addLocation(mahdia)
        repository.addLocation(ariana)
        selectionStore.setSelectedLocationId(9_999L)

        val active = repository.getActiveLocation().first()

        assertEquals("Ariana", active?.name)
    }

    @Test
    fun setActiveLocation_switchesTheActiveLocation() = runTest {
        val mahdiaId = repository.addLocation(mahdia)
        val arianaId = repository.addLocation(ariana)

        repository.setActiveLocation(mahdiaId)
        assertEquals(mahdiaId, repository.getActiveLocation().first()?.id)

        repository.setActiveLocation(arianaId)
        assertEquals(arianaId, repository.getActiveLocation().first()?.id)
        assertEquals(arianaId, selectionStore.currentId)
    }

    @Test
    fun addLocation_doesNotInsertAnExactDuplicate() = runTest {
        val firstId = repository.addLocation(mahdia)
        val secondId = repository.addLocation(mahdia)

        assertEquals(firstId, secondId)
        assertEquals(1, dao.getLocationCount())
    }

    @Test
    fun deleteSelectedLocation_selectsRemainingRow() = runTest {
        val mahdiaId = repository.addLocation(mahdia)
        val arianaId = repository.addLocation(ariana)
        repository.setActiveLocation(mahdiaId)

        assertTrue(repository.deleteLocation(repository.getLocationById(mahdiaId)!!))

        assertEquals(arianaId, repository.getActiveLocation().first()?.id)
        assertEquals(arianaId, selectionStore.currentId)
        assertEquals(1, dao.getLocationCount())
    }

    @Test
    fun deleteLastLocation_clearsSelection() = runTest {
        val id = repository.addLocation(mahdia)
        repository.setActiveLocation(id)

        assertTrue(repository.deleteLocation(repository.getLocationById(id)!!))

        assertNull(repository.getActiveLocation().first())
        assertNull(selectionStore.currentId)
    }
}
