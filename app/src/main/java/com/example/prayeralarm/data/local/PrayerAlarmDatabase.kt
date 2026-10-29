package com.example.prayeralarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.prayeralarm.data.local.dao.LocationDao
import com.example.prayeralarm.data.local.entity.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Main Room SQLite database definition for the Prayer Alarm application.
 */
@Database(
    entities = [LocationEntity::class],
    version = 2,
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
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        suspend fun populateInitialLocations(dao: LocationDao): Long {
            return dao.insertLocations(DefaultLocations.all).first()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                DefaultLocations.all.forEach { location ->
                    database.execSQL(
                        """
                        INSERT INTO saved_locations (name, latitude, longitude, timezone_id)
                        SELECT ?, ?, ?, ?
                        WHERE NOT EXISTS (
                            SELECT 1 FROM saved_locations
                            WHERE name = ? AND latitude = ? AND longitude = ? AND timezone_id = ?
                        )
                        """.trimIndent(),
                        arrayOf(
                            location.name,
                            location.latitude,
                            location.longitude,
                            location.timeZoneId,
                            location.name,
                            location.latitude,
                            location.longitude,
                            location.timeZoneId
                        )
                    )
                }
            }
        }
    }
}
