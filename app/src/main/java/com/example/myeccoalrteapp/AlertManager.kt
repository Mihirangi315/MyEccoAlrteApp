package com.example.myeccoalrteapp

import android.content.Context
import android.util.Log

/**
 * Skeleton class for the Alert layer.
 * To be implemented by the Alert team member (Vibration + Notifications).
 */
object AlertManager {
    /**
     * Triggers a user alert when a target sound is detected.
     */
    fun trigger(context: Context, soundLabel: String) {
        Log.i("AlertManager", "ALERT TRIGGERED: $soundLabel detected!")
        // This will be replaced by Notification and Vibrator logic
    }
}
