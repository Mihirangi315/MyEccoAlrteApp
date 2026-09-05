package com.example.myeccoalrteapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat

object AlertManager {

    private const val CHANNEL_ID = "DetectionAlertChannel"
    private const val CHANNEL_NAME = "Sound Detections"
    
    private var activeVibrator: Vibrator? = null

    fun trigger(context: Context, soundLabel: String, playSound: Boolean = false, isContinuous: Boolean = false) {
        Log.i("AlertManager", "Triggering alert for: $soundLabel (Sound: $playSound, Continuous: $isContinuous)")
        
        startVibration(context, isContinuous)
        
        if (playSound) {
            playAlertSound(context)
        }
        showNotification(context, soundLabel, isContinuous)
    }

    fun stopVibration() {
        Log.d("AlertManager", "Stopping all active vibrations")
        activeVibrator?.cancel()
        activeVibrator = null
    }

    private fun startVibration(context: Context, isContinuous: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        activeVibrator = vibrator

        if (isContinuous) {
            // Pattern: [Wait 0ms, Vibrate 1000ms, Wait 500ms, Vibrate 1000ms...]
            val pattern = longArrayOf(0, 1000, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1))
        } else {
            // Strong single pulse
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun playAlertSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone.play()
        } catch (e: Exception) {
            Log.e("AlertManager", "Error playing sound: ${e.message}")
        }
    }

    private fun showNotification(context: Context, soundLabel: String, isContinuous: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for detected sounds"
            enableVibration(true)
            // Ensure notification can break through Do Not Disturb
            setBypassDnd(true)
            setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
        }
        notificationManager.createNotificationChannel(channel)

        val dismissIntent = Intent(context, ListeningService::class.java).apply {
            action = "ACTION_DISMISS_ALERT"
        }
        val dismissPendingIntent = PendingIntent.getService(
            context, 0, dismissIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_listening_active)
            .setContentTitle("CRITICAL SOUND: $soundLabel")
            .setContentText("Tap DISMISS to stop the alert.")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Use MAX for overlay/heads-up
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true) // Increases visibility
            .setAutoCancel(true)

        if (isContinuous) {
            builder.addAction(R.drawable.ic_back, "DISMISS", dismissPendingIntent)
            builder.setOngoing(true)
        }

        // Using a fixed ID (1001) so we can dismiss it easily from the service
        notificationManager.notify(1001, builder.build())
    }
}
