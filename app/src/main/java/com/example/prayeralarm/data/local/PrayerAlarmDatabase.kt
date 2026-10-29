package com.example.prayeralarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.prayeralarm.data.local.dao.LocationDao
import com.example.prayeralarm.data.local.entity.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room SQLite database definition for the Prayer Alarm application.
 */
@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrayerAlarmDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

    companion object {
        private const val DATABASE_NAME = "prayer_alarm.db"

        @Volatile
        private var INSTANCE: PrayerAlarmDatabase? = null

        /**
         * Singleton accessor for the database instance.
         */
        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): PrayerAlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext, scope).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context, scope: CoroutineScope): PrayerAlarmDatabase {
            return Room.databaseBuilder(
                context,
                PrayerAlarmDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabasePrepopulateCallback(scope))
                .build()
        }

        /**
         * Prepopulates standard Tunisian governorate cities upon first database creation.
         */
        private class DatabasePrepopulateCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialLocations(database.locationDao())
                    }
                }
            }
        }

        /**
         * Seeds initial Tunisian benchmark locations with validated coordinates and IANA timezones.
         */
        suspend fun populateInitialLocations(dao: LocationDao) {
            val defaults = listOf(
                LocationEntity(name = "Tunis", latitude = 36.8065, longitude = 10.1815, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Sfax", latitude = 34.7406, longitude = 10.7603, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Sousse", latitude = 35.8256, longitude = 10.6369, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Kairouan", latitude = 35.6781, longitude = 10.0963, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Bizerte", latitude = 37.2744, longitude = 9.8739, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Gabès", latitude = 33.8815, longitude = 10.0982, timeZoneId = "Africa/Tunis"),
                LocationEntity(name = "Tataouine", latitude = 32.9297, longitude = 10.4518, timeZoneId = "Africa/Tunis")
            )
            dao.insertLocations(defaults)
        }
    }
}

