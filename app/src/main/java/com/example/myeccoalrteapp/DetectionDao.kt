package com.example.myeccoalrteapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * A @Dao (Data Access Object) is an interface where we define 
 * our database queries using SQL or Room annotations.
 */
@Dao
interface DetectionDao {

    /**
     * 'suspend' means this function can be paused and resumed. 
     * Room uses this to ensure database work happens on a background thread
     * so it doesn't "freeze" your app's UI.
     */
    @Insert
    suspend fun insert(event: DetectionEvent)

    /**
     * Retrieves all detections, newest first.
     * We return a Flow so that the UI can automatically update whenever 
     * a new detection is added to the database.
     */
    @Query("SELECT * FROM detections ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionEvent>>

    /**
     * Clears all history.
     */
    @Query("DELETE FROM detections")
    suspend fun clearAll()
}
