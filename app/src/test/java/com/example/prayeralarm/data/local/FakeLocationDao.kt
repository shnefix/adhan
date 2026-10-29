package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.dao.LocationDao
import com.example.prayeralarm.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake DAO for fast JVM unit tests.
 *
 * It mimics the production query order (`ORDER BY name ASC`) and auto-generated IDs.
 */
class FakeLocationDao : LocationDao {
    private val storage = mutableMapOf<Long, LocationEntity>()
    private val stateFlow = MutableStateFlow<List<LocationEntity>>(emptyList())
    private var idCounter = 1L

    private fun emit() {
        stateFlow.value = storage.values.sortedBy { it.name }
    }

    override fun getAllLocations(): Flow<List<LocationEntity>> = stateFlow.asStateFlow()

    override suspend fun getLocationById(id: Long): LocationEntity? = storage[id]

    override suspend fun getLocationCount(): Int = storage.size

    override suspend fun findByIdentity(
        name: String,
        latitude: Double,
        longitude: Double,
        timeZoneId: String
    ): LocationEntity? {
        return storage.values.firstOrNull {
            it.name == name &&
                it.latitude == latitude &&
                it.longitude == longitude &&
                it.timeZoneId == timeZoneId
        }
    }

    override suspend fun insertLocation(location: LocationEntity): Long {
        val assignedId = if (location.id == 0L) idCounter++ else location.id
        val entity = location.copy(id = assignedId)
        storage[assignedId] = entity
        emit()
        return assignedId
    }

    override suspend fun insertLocations(locations: List<LocationEntity>): List<Long> {
        return locations.map { insertLocation(it) }
    }

    override suspend fun updateLocation(location: LocationEntity): Int {
        return if (storage.containsKey(location.id)) {
            storage[location.id] = location
            emit()
            1
        } else {
            0
        }
    }

    override suspend fun deleteLocation(location: LocationEntity): Int {
        return deleteLocationById(location.id)
    }

    override suspend fun deleteLocationById(id: Long): Int {
        return if (storage.remove(id) != null) {
            emit()
            1
        } else {
            0
        }
    }
}
