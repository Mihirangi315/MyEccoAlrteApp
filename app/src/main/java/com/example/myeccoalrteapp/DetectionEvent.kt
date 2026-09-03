package com.example.myeccoalrteapp

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An @Entity represents a table in our Room database.
 * Each instance of this class will be a row in the 'detections' table.
 */
@Entity(tableName = "detections")
data class DetectionEvent(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0, // Room will automatically generate unique IDs for each record
    val soundLabel: String, // The name of the sound detected
    val timestamp: Long // We store time as a Long (milliseconds) for easier sorting
)
