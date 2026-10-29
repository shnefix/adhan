package com.example.prayeralarm.data.repository

import com.example.prayeralarm.data.local.dao.LocationDao
import com.example.prayeralarm.data.local.entity.toDomain
import com.example.prayeralarm.data.local.entity.toEntity
import com.example.prayeralarm.data.preferences.LocationSelectionStore
import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [LocationRepository] coordinating Room database and DataStore preferences.
 */
class LocationRepositoryImpl(
    private val locationDao: LocationDao,
    private val selectionStore: LocationSelectionStore
) : LocationRepository {

    override fun getAllLocations(): Flow<List<SavedLocation>> {
        return combine(
            locationDao.getAllLocations(),
            selectionStore.selectedLocationId
        ) { entities, selectedId ->
            entities.map { entity ->
                entity.toDomain(isSelected = (entity.id == selectedId))
            }
        }
    }

    override fun getActiveLocation(): Flow<SavedLocation?> {
        return combine(
            locationDao.getAllLocations(),
            selectionStore.selectedLocationId
        ) { entities, selectedId ->
            if (entities.isEmpty()) {
                null
            } else {
                // Return explicitly selected location or default to first location in list
                val activeEntity = entities.find { it.id == selectedId } ?: entities.first()
                activeEntity.toDomain(isSelected = true)
            }
        }
    }

    override suspend fun getLocationById(id: Long): SavedLocation? {
        return locationDao.getLocationById(id)?.toDomain()
    }

    override suspend fun addLocation(location: SavedLocation): Long {
        val existing = locationDao.findByIdentity(
            name = location.name,
            latitude = location.latitude,
            longitude = location.longitude,
            timeZoneId = location.timeZoneId
        )
        if (existing != null) {
            if (location.isDefault) {
                selectionStore.setSelectedLocationId(existing.id)
            }
            return existing.id
        }

        val entity = location.toEntity()
        val newId = locationDao.insertLocation(entity)
        // If this is set as default or is the first inserted location, set as active
        if (location.isDefault) {
            selectionStore.setSelectedLocationId(newId)
        }
        return newId
    }

    override suspend fun updateLocation(location: SavedLocation): Boolean {
        val rows = locationDao.updateLocation(location.toEntity())
        return rows > 0
    }

    override suspend fun deleteLocation(location: SavedLocation): Boolean {
        val selectedId = selectionStore.selectedLocationId.first()
        val rows = locationDao.deleteLocationById(location.id)
        if (rows > 0 && selectedId == location.id) {
            val replacement = locationDao.getAllLocations().first().firstOrNull()
            if (replacement == null) {
                selectionStore.clearSelectedLocationId()
            } else {
                selectionStore.setSelectedLocationId(replacement.id)
            }
        }
        return rows > 0
    }

    override suspend fun setActiveLocation(locationId: Long) {
        selectionStore.setSelectedLocationId(locationId)
    }
}
