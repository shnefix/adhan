package com.example.prayeralarm.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory selection store used by repository and seeder tests. */
class FakeLocationSelectionStore(initialId: Long? = null) : LocationSelectionStore {
    private val state = MutableStateFlow(initialId)

    override val selectedLocationId: Flow<Long?> = state.asStateFlow()

    val currentId: Long?
        get() = state.value

    override suspend fun setSelectedLocationId(locationId: Long) {
        state.value = locationId
    }

    override suspend fun clearSelectedLocationId() {
        state.value = null
    }
}
