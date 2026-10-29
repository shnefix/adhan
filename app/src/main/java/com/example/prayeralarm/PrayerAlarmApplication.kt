package com.example.prayeralarm

import android.app.Application
import com.example.prayeralarm.data.local.PrayerAlarmDatabase
import com.example.prayeralarm.data.preferences.UserPreferencesManager
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.data.repository.LocationRepositoryImpl
import com.example.prayeralarm.alarm.NotificationChannels
import com.example.prayeralarm.ui.alarms.AlarmPlanPreferences
import com.example.prayeralarm.ui.alarms.DataStoreAlarmPlanPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class for Prayer Alarm.
 *
 * Serves as the central service locator / manual dependency injection container
 * for long-lived application components (Room database, repositories, preferences).
 */
class PrayerAlarmApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: PrayerAlarmDatabase by lazy {
        PrayerAlarmDatabase.getInstance(this, applicationScope)
    }

    val preferencesManager: UserPreferencesManager by lazy {
        UserPreferencesManager(this)
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(
            locationDao = database.locationDao(),
            preferencesManager = preferencesManager
        )
    }

    val alarmPlanPreferences: AlarmPlanPreferences by lazy {
        DataStoreAlarmPlanPreferences(preferencesManager)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        // Ensure default locations are seeded if empty
        applicationScope.launch {
            if (database.locationDao().getLocationCount() == 0) {
                val locationId = PrayerAlarmDatabase.populateInitialLocations(database.locationDao())
                preferencesManager.setSelectedLocationId(locationId)
            }
        }
    }
}
