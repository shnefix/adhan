package com.example.prayeralarm.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.domain.model.SavedLocation
import com.example.prayeralarm.util.LocationValidator
import com.example.prayeralarm.util.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing location list presentation, editing, deletion with confirmation,
 * coordinate validation, and active selection.
 */
class LocationsViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationsUiState(isLoading = true))
    val uiState: StateFlow<LocationsUiState> = _uiState.asStateFlow()

    init {
        observeLocations()
    }

    private fun observeLocations() {
        viewModelScope.launch {
            combine(
                repository.getAllLocations(),
                repository.getActiveLocation()
            ) { locations, active ->
                Pair(locations, active)
            }.collect { (locations, active) ->
                _uiState.update { current ->
                    current.copy(
                        locations = locations,
                        activeLocation = active,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.setActiveLocation(location.id)
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddEditDialog = true, editingLocation = null) }
    }

    fun openEditDialog(location: SavedLocation) {
        _uiState.update { it.copy(showAddEditDialog = true, editingLocation = location) }
    }

    fun dismissAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, editingLocation = null) }
    }

    fun saveLocation(
        name: String,
        latitudeStr: String,
        longitudeStr: String,
        timeZoneId: String
    ): ValidationResult {
        val validation = LocationValidator.validate(
            name = name,
            latitudeStr = latitudeStr,
            longitudeStr = longitudeStr,
            timezoneId = timeZoneId
        )

        if (!validation.isValid) {
            return validation
        }

        val lat = latitudeStr.trim().toDouble()
        val lon = longitudeStr.trim().toDouble()
        val tz = timeZoneId.trim()
        val locName = name.trim()

        viewModelScope.launch {
            val currentEditing = _uiState.value.editingLocation
            if (currentEditing != null) {
                // Update
                val updated = currentEditing.copy(
                    name = locName,
                    latitude = lat,
                    longitude = lon,
                    timeZoneId = tz
                )
                repository.updateLocation(updated)
            } else {
                // Add
                val isFirst = _uiState.value.locations.isEmpty()
                val newLocation = SavedLocation(
                    name = locName,
                    latitude = lat,
                    longitude = lon,
                    timeZoneId = tz,
                    isDefault = isFirst
                )
                repository.addLocation(newLocation)
            }
            dismissAddEditDialog()
        }

        return validation
    }

    fun requestDeleteLocation(location: SavedLocation) {
        _uiState.update { it.copy(locationToDelete = location) }
    }

    fun cancelDeleteLocation() {
        _uiState.update { it.copy(locationToDelete = null) }
    }

    fun confirmDeleteLocation() {
        val location = _uiState.value.locationToDelete ?: return
        viewModelScope.launch {
            repository.deleteLocation(location)
            _uiState.update { it.copy(locationToDelete = null) }
        }
    }

    class Factory(private val repository: LocationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationsViewModel::class.java)) {
                return LocationsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

