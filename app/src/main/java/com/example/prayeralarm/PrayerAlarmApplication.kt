package com.example.prayeralarm

import android.app.Application
import com.example.prayeralarm.data.local.PrayerAlarmDatabase
import com.example.prayeralarm.data.preferences.UserPreferencesManager
import com.example.prayeralarm.data.repository.LocationRepository
import com.example.prayeralarm.data.repository.LocationRepositoryImpl
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

    override fun onCreate() {
        super.onCreate()
        // Ensure default locations are seeded if empty
        applicationScope.launch {
            if (database.locationDao().getLocationCount() == 0) {
                PrayerAlarmDatabase.populateInitialLocations(database.locationDao())
            }
        }
    }
}
