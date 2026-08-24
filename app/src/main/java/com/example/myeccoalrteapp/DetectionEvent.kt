package com.example.myeccoalrteapp

import androidx.annotation.DrawableRes

/**
 * A simple data class to represent a single sound detection event.
 * Enhanced with an icon resource ID for a better UI.
 */
data class DetectionEvent(
    val soundName: String,
    val timestamp: String,
    @DrawableRes val iconResId: Int // Resource ID for the sound's icon
)
