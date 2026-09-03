package com.example.myeccoalrteapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SoundSettingDao {

    /**
     * Inserts or updates a setting. 
     * REPLACE strategy means if the soundLabel exists, it overwrites it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SoundSetting)

    @Query("SELECT * FROM sound_settings")
    suspend fun getAllSettings(): List<SoundSetting>

    @Query("SELECT * FROM sound_settings WHERE soundLabel = :label LIMIT 1")
    suspend fun getSettingForSound(label: String): SoundSetting?
}
