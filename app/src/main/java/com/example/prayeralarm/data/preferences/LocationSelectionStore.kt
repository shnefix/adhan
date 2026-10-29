package com.example.prayeralarm.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Persistence boundary for the active location selection.
 *
 * Extracted from [UserPreferencesManager] so seeding, migration repair, and
 * repository selection logic can be unit-tested on the JVM without an Android
 * `Context` or a real DataStore instance.
 */
interface LocationSelectionStore {

    /** Emits the selected location ID, or null when no selection is stored. */
    val selectedLocationId: Flow<Long?>

    /** Persists the selected location ID. */
    suspend fun setSelectedLocationId(locationId: Long)

    /** Removes the stored selection. */
    suspend fun clearSelectedLocationId()
}
