package com.example.myeccoalrteapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * AlertManager handles telling the user that a sound was detected
 * through physical vibration and a high-priority notification.
 */
object AlertManager {

    private const val CHANNEL_ID = "DetectionAlertChannel"
    private const val CHANNEL_NAME = "Sound Detections"

    /**
     * Triggers both a vibration and a notification.
     */
    fun trigger(context: Context, soundLabel: String) {
        Log.i("AlertManager", "Triggering alert for: $soundLabel")
        
        vibratePhone(context)
        showNotification(context, soundLabel)
    }

    /**
     * Vibrates the device for 800 milliseconds.
     */
    private fun vibratePhone(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /**
     * Shows a high-priority notification in the system drawer.
     */
    private fun showNotification(context: Context, soundLabel: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the channel if it doesn't exist (Required for Android 8.0+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for detected sounds like alarms or doorbells"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_listening_active)
            .setContentTitle("Sound Detected!")
            .setContentText("EchoAlert identified a $soundLabel")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        // We use a unique ID for each notification so they don't overwrite each other
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
