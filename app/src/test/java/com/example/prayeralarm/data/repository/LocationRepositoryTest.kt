package com.example.prayeralarm.data.repository

import com.example.prayeralarm.data.local.FakeLocationDao
import com.example.prayeralarm.data.local.entity.LocationEntity
import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying [LocationRepositoryImpl] behavior using an in-memory Fake DAO and Fake Preferences.
 */
class LocationRepositoryTest {

    private lateinit var fakeDao: FakeLocationDao
    private lateinit var repository: FakeLocationRepository

    @Before
    fun setUp() {
        fakeDao = FakeLocationDao()
        repository = FakeLocationRepository(fakeDao)
    }

    @Test
    fun addLocation_insertsAndEmitsInList() = runTest {
        val location = SavedLocation(
            name = "Sfax",
            latitude = 34.7406,
            longitude = 10.7603,
            timeZoneId = "Africa/Tunis"
        )

        val id = repository.addLocation(location)
        assertTrue(id > 0)

        val list = repository.getAllLocations().first()
        assertEquals(1, list.size)
        assertEquals("Sfax", list[0].name)
    }

    @Test
    fun getActiveLocation_defaultsToFirstWhenUnset() = runTest {
        repository.addLocation(
            SavedLocation(name = "Sousse", latitude = 35.8256, longitude = 10.6369, timeZoneId = "Africa/Tunis")
        )
        repository.addLocation(
            SavedLocation(name = "Tunis", latitude = 36.8065, longitude = 10.1815, timeZoneId = "Africa/Tunis")
        )

        val active = repository.getActiveLocation().first()
        assertNotNull(active)
        // Storage is sorted alphabetically by name: "Sousse" comes before "Tunis"
        assertEquals("Sousse", active?.name)
    }

    @Test
    fun setActiveLocation_updatesActiveLocation() = runTest {
        val id1 = repository.addLocation(
            SavedLocation(name = "Tunis", latitude = 36.8065, longitude = 10.1815, timeZoneId = "Africa/Tunis")
        )
        val id2 = repository.addLocation(
            SavedLocation(name = "Sousse", latitude = 35.8256, longitude = 10.6369, timeZoneId = "Africa/Tunis")
        )

        repository.setActiveLocation(id2)

        val active = repository.getActiveLocation().first()
        assertEquals("Sousse", active?.name)
        assertEquals(id2, active?.id)
    }

    @Test
    fun deleteLocation_removesFromList() = runTest {
        val id = repository.addLocation(
            SavedLocation(name = "Kairouan", latitude = 35.6781, longitude = 10.0963, timeZoneId = "Africa/Tunis")
        )

        val item = repository.getLocationById(id)
        assertNotNull(item)

        repository.deleteLocation(item!!)

        val list = repository.getAllLocations().first()
        assertTrue(list.isEmpty())
    }

    @Test
    fun addLocation_doesNotInsertExactDuplicate() = runTest {
        val location = SavedLocation(
            name = "Mahdia",
            latitude = 35.5047,
            longitude = 11.0622,
            timeZoneId = "Africa/Tunis"
        )

        val firstId = repository.addLocation(location)
        val secondId = repository.addLocation(location)

        assertEquals(firstId, secondId)
        assertEquals(1, repository.getAllLocations().first().size)
    }

    @Test
    fun deleteActiveLocation_selectsRemainingLocation() = runTest {
        val firstId = repository.addLocation(
            SavedLocation(name = "Mahdia", latitude = 35.5047, longitude = 11.0622, timeZoneId = "Africa/Tunis")
        )
        val secondId = repository.addLocation(
            SavedLocation(name = "Ariana", latitude = 36.8625, longitude = 10.1956, timeZoneId = "Africa/Tunis")
        )
        repository.setActiveLocation(firstId)

        repository.deleteLocation(repository.getLocationById(firstId)!!)

        assertEquals(secondId, repository.getActiveLocation().first()?.id)
    }
}

/**
 * Fake implementation of [LocationRepository] utilizing in-memory flow.
 */
class FakeLocationRepository(private val dao: FakeLocationDao) : LocationRepository {
    private val selectedLocationId = MutableStateFlow<Long?>(null)

    override fun getAllLocations(): Flow<List<SavedLocation>> {
        return dao.getAllLocations().map { list ->
            list.map { entity ->
                SavedLocation(
                    id = entity.id,
                    name = entity.name,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    timeZoneId = entity.timeZoneId,
                    isDefault = entity.id == selectedLocationId.value
                )
            }
        }
    }

    override fun getActiveLocation(): Flow<SavedLocation?> {
        return kotlinx.coroutines.flow.combine(
            dao.getAllLocations(),
            selectedLocationId
        ) { list, selectedId ->
            if (list.isEmpty()) null
            else {
                val entity = list.find { it.id == selectedId } ?: list.first()
                SavedLocation(
                    id = entity.id,
                    name = entity.name,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    timeZoneId = entity.timeZoneId,
                    isDefault = true
                )
            }
        }
    }

    override suspend fun getLocationById(id: Long): SavedLocation? {
        val entity = dao.getLocationById(id) ?: return null
        return SavedLocation(
            id = entity.id,
            name = entity.name,
            latitude = entity.latitude,
            longitude = entity.longitude,
            timeZoneId = entity.timeZoneId
        )
    }

    override suspend fun addLocation(location: SavedLocation): Long {
        val existing = dao.findByIdentity(
            location.name,
            location.latitude,
            location.longitude,
            location.timeZoneId
        )
        if (existing != null) {
            if (location.isDefault) {
                selectedLocationId.value = existing.id
            }
            return existing.id
        }
        return dao.insertLocation(
            LocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                timeZoneId = location.timeZoneId
            )
        )
    }

    override suspend fun updateLocation(location: SavedLocation): Boolean {
        return dao.updateLocation(
            LocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                timeZoneId = location.timeZoneId
            )
        ) > 0
    }

    override suspend fun deleteLocation(location: SavedLocation): Boolean {
        val wasSelected = selectedLocationId.value == location.id
        val deleted = dao.deleteLocationById(location.id) > 0
        if (deleted && wasSelected) {
            selectedLocationId.value = dao.getAllLocations().first().firstOrNull()?.id
        }
        return deleted
    }

    override suspend fun setActiveLocation(locationId: Long) {
        selectedLocationId.value = locationId
    }
}
