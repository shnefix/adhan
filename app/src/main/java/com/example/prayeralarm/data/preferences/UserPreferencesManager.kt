package com.example.prayeralarm.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_alarm_preferences")

/**
 * Manages key-value application settings using Jetpack DataStore Preferences.
 */
class UserPreferencesManager(private val context: Context) : LocationSelectionStore {

    companion object {
        val KEY_SELECTED_LOCATION_ID = longPreferencesKey("selected_location_id")
        val KEY_CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        private val KEY_ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        private val KEY_ALARM_COUNT = longPreferencesKey("alarm_count")
        private val KEY_ALARM_INTERVAL_MINUTES = longPreferencesKey("alarm_interval_minutes")
        private val KEY_ALARM_TIMING_MODE = stringPreferencesKey("alarm_timing_mode")
    private val KEY_ENABLE_PERSISTENT_NOTIFICATION = booleanPreferencesKey("enable_persistent_notification")
    }

    val calculationMethod: Flow<com.example.prayeralarm.domain.calculator.PrayerCalculationMethod> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_CALCULATION_METHOD]?.let { name ->
                runCatching { com.example.prayeralarm.domain.calculator.PrayerCalculationMethod.valueOf(name) }.getOrNull()
            } ?: com.example.prayeralarm.domain.calculator.PrayerCalculationMethod.DEFAULT
        }

    suspend fun setCalculationMethod(method: com.example.prayeralarm.domain.calculator.PrayerCalculationMethod) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CALCULATION_METHOD] = method.name
        }
    }

    /**
     * Observes the active location ID. Emits null if no location has been selected.
     */
    override val selectedLocationId: Flow<Long?> = context.dataStore.data
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
    override suspend fun setSelectedLocationId(locationId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_LOCATION_ID] = locationId
        }
    }

    /**
     * Clears the active location ID.
     */
    override suspend fun clearSelectedLocationId() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SELECTED_LOCATION_ID)
        }
    }

    val alarmPlanConfig: Flow<AlarmPlanConfig> = context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val count = preferences[KEY_ALARM_COUNT]?.toInt()
                val interval = preferences[KEY_ALARM_INTERVAL_MINUTES]
                val mode = preferences[KEY_ALARM_TIMING_MODE]?.let {
                    runCatching { AlarmTimingMode.valueOf(it) }.getOrNull()
                }
                AlarmPlanConfig(
                    alarmCount = count ?: AlarmPlanConfig.DEFAULT.alarmCount,
                    intervalMinutes = interval ?: AlarmPlanConfig.DEFAULT.intervalMinutes,
                    timingMode = mode ?: AlarmPlanConfig.DEFAULT.timingMode,
                    enabled = preferences[KEY_ALARM_ENABLED] ?: AlarmPlanConfig.DEFAULT.enabled
                )
            }

    suspend fun setAlarmPlanConfig(config: AlarmPlanConfig) {
        require(config.alarmCount > 0) { "Alarm count must be positive." }
        require(config.intervalMinutes > 0) { "Alarm interval must be positive." }
        context.dataStore.edit { preferences ->
            preferences[KEY_ALARM_ENABLED] = config.enabled
            preferences[KEY_ALARM_COUNT] = config.alarmCount.toLong()
            preferences[KEY_ALARM_INTERVAL_MINUTES] = config.intervalMinutes
            preferences[KEY_ALARM_TIMING_MODE] = config.timingMode.name
        }
    }
}
