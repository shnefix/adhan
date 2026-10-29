package com.example.prayeralarm.data.repository

import com.example.prayeralarm.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

/**
 * Clean architecture boundary contract for location persistence operations.
 */
interface LocationRepository {

    /**
     * Observes all saved locations mapped to domain models.
     */
    fun getAllLocations(): Flow<List<SavedLocation>>

    /**
     * Observes the currently selected active location.
     * Emits the selected location or falls back to the first available location if the selection is unset or invalid.
     */
    fun getActiveLocation(): Flow<SavedLocation?>

    /**
     * Retrieves a location by its ID.
     */
    suspend fun getLocationById(id: Long): SavedLocation?

    /**
     * Adds a new location and returns the new location ID.
     */
    suspend fun addLocation(location: SavedLocation): Long

    /**
     * Updates an existing location's metadata.
     */
    suspend fun updateLocation(location: SavedLocation): Boolean

    /**
     * Deletes a location by its ID.
     */
    suspend fun deleteLocation(location: SavedLocation): Boolean

    /**
     * Sets the active location ID in DataStore.
     */
    suspend fun setActiveLocation(locationId: Long)
}

