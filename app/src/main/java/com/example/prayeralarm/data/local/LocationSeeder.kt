package com.example.prayeralarm.data.local

import com.example.prayeralarm.data.local.dao.LocationDao
import com.example.prayeralarm.data.preferences.LocationSelectionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Outcome of a startup seed/selection repair pass.
 *
 * @property seeded True only when an empty database was populated with [DefaultLocations]
 * @property selectedLocationId The selection after the pass (null only if no rows exist)
 * @property repairedSelection True when an unset or dangling selection was replaced
 */
data class LocationSeedResult(
    val seeded: Boolean,
    val selectedLocationId: Long?,
    val repairedSelection: Boolean = false
)

/**
 * Seeds the three Tunisian defaults exactly once and repairs a broken selection.
 *
 * Upgrade safety:
 * - Existing rows are never deleted, updated, or re-inserted.
 * - Seeding runs only when the table is completely empty.
 * - A valid existing selection is preserved.
 * - Only an unset selection, or one pointing at a deleted row, is replaced.
 */
class LocationSeeder(
    private val locationDao: LocationDao,
    private val selectionStore: LocationSelectionStore
) {
    private val mutex = Mutex()

    suspend fun seedIfNeeded(): LocationSeedResult = mutex.withLock {
        if (locationDao.getLocationCount() == 0) {
            val insertedIds = locationDao.insertLocations(DefaultLocations.all)
            // insertLocations returns IDs in input order, so the first ID is Mahdia.
            val primaryId = insertedIds.firstOrNull()
                ?: locationDao.getAllLocations().first().firstOrNull()?.id
            if (primaryId != null) {
                selectionStore.setSelectedLocationId(primaryId)
            }
            return@withLock LocationSeedResult(
                seeded = true,
                selectedLocationId = primaryId
            )
        }

        repairSelectionIfNeeded()
    }

    private suspend fun repairSelectionIfNeeded(): LocationSeedResult {
        val selectedId = selectionStore.selectedLocationId.first()
        if (selectedId != null && locationDao.getLocationById(selectedId) != null) {
            return LocationSeedResult(
                seeded = false,
                selectedLocationId = selectedId
            )
        }

        // DAO orders by name, matching LocationRepositoryImpl's fallback.
        val fallback = locationDao.getAllLocations().first().firstOrNull()
            ?: return LocationSeedResult(seeded = false, selectedLocationId = null)

        selectionStore.setSelectedLocationId(fallback.id)
        return LocationSeedResult(
            seeded = false,
            selectedLocationId = fallback.id,
            repairedSelection = true
        )
    }
}
