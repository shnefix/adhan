package com.example.prayeralarm.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_alarm_preferences")

/**
 * Manages key-value application settings using Jetpack DataStore Preferences.
 */
class UserPreferencesManager(private val context: Context) {

    companion object {
        val KEY_SELECTED_LOCATION_ID = longPreferencesKey("selected_location_id")
    }

    /**
     * Observes the active location ID. Emits null if no location has been selected.
     */
    val selectedLocationId: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SELECTED_LOCATION_ID]
        }

    /**
     * Saves the active location ID.
     */
    suspend fun setSelectedLocationId(locationId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_LOCATION_ID] = locationId
        }
    }

    /**
     * Clears the active location ID.
     */
    suspend fun clearSelectedLocationId() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SELECTED_LOCATION_ID)
        }
    }
}

