package com.example.prayeralarm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.prayeralarm.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [LocationEntity] database interactions.
 *
 * Provides reactive query capabilities via Kotlin [Flow] and asynchronous
 * CRUD operations via Kotlin coroutines [suspend] functions.
 */
@Dao
interface LocationDao {

    /**
     * Observes all saved locations ordered alphabetically by name.
     * Emits a new list whenever the database table changes.
     */
    @Query("SELECT * FROM saved_locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    /**
     * Retrieves a single location by its primary key ID.
     */
    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Long): LocationEntity?

    /**
     * Retrieves the count of saved locations in the database.
     */
    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getLocationCount(): Int

    @Query(
        "SELECT * FROM saved_locations " +
            "WHERE name = :name AND latitude = :latitude AND longitude = :longitude " +
            "AND timezone_id = :timeZoneId LIMIT 1"
    )
    suspend fun findByIdentity(
        name: String,
        latitude: Double,
        longitude: Double,
        timeZoneId: String
    ): LocationEntity?

    /**
     * Inserts a location. If a conflict occurs, replaces existing record.
     * @return Row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    /**
     * Inserts a list of locations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>): List<Long>

    /**
     * Updates an existing location.
     */
    @Update
    suspend fun updateLocation(location: LocationEntity): Int

    /**
     * Deletes a specific location entity.
     */
    @Delete
    suspend fun deleteLocation(location: LocationEntity): Int

    /**
     * Deletes a location by its ID.
     */
    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long): Int
}
