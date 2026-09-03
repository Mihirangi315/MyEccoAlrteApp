package com.example.myeccoalrteapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class for our app. 
 * We list all our entities here and provide access to the DAOs.
 */
@Database(entities = [DetectionEvent::class, SoundSetting::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun detectionDao(): DetectionDao
    abstract fun soundSettingDao(): SoundSettingDao

    companion object {
        // @Volatile ensures the instance is always up-to-date across all threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton pattern: This function ensures that only ONE connection
         * to the database is created and shared across the entire app.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "echo_alert_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
