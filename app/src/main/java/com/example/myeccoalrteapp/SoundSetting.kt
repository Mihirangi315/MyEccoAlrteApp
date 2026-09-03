package com.example.myeccoalrteapp

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This entity stores the user's preferences for each sound type.
 */
@Entity(tableName = "sound_settings")
data class SoundSetting(
    @PrimaryKey
    val soundLabel: String, // e.g., "Doorbell", "Alarm"
    val isEnabled: Boolean = true, // Whether the user wants alerts for this sound
    val sensitivity: Float = 0.5f // Detection threshold for this specific sound
)
