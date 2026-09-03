package com.example.myeccoalrteapp

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

/**
 * A Foreground Service is required because we need the app to keep recording audio
 * even if the user switches to another app or locks their screen.
 * Regular background threads are often killed by Android to save battery.
 */
class ListeningService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // Reference to the database to save detections
    private lateinit var database: AppDatabase

    // Configuration for AudioRecord (Required for YAMNet model)
    private val sampleRate = 16000 // 16kHz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    companion object {
        private const val CHANNEL_ID = "EchoAlertChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Initialize the ML model brain
        SoundClassifier.initialize(this)
        // Get the singleton database instance
        database = AppDatabase.getDatabase(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ListeningService", "Service Started")
        
        // Start the service in the Foreground immediately
        startAsForeground()
        
        // Start the audio capture loop
        startListening()

        return START_STICKY // Tells Android to restart the service if it gets killed
    }

    /**
     * Shows the required persistent notification to the user.
     */
    private fun startAsForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EchoAlert is Listening")
            .setContentText("Monitoring for important sounds around you.")
            .setSmallIcon(R.drawable.ic_listening_active)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startListening() {
        if (isRecording) return
        
        // Verify permission again just in case (though activity should handle it)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        try {
            // Step 1: Initialize AudioRecord
            // We use MIC as the source and the 16kHz Mono 16-bit format
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("ListeningService", "AudioRecord initialization failed")
                return
            }

            isRecording = true
            audioRecord?.startRecording()

            // Step 2: Start a background loop to read data
            serviceScope.launch {
                val audioBuffer = ShortArray(sampleRate) // ~1 second of audio at 16kHz
                val floatBuffer = FloatArray(sampleRate)

                while (isActive && isRecording) {
                    // Step 3: Read data from the microphone into our buffer
                    val readResult = audioRecord?.read(audioBuffer, 0, audioBuffer.size)

                    if (readResult != null && readResult > 0) {
                        // Step 4: Convert ShortArray (PCM 16-bit) to FloatArray (-1.0 to 1.0)
                        // YAMNet expects normalized float values.
                        for (i in 0 until readResult) {
                            floatBuffer[i] = audioBuffer[i] / 32768.0f
                        }

                        // Step 5: Pass the buffer to the ML Layer (SoundClassifier)
                        val result = SoundClassifier.classify(floatBuffer)
                        
                        // Step 6: If a sound is detected, trigger the Alert Layer and save to DB
                        result?.let { (label, confidence) ->
                            // 1. Alert the user immediately
                            AlertManager.trigger(this@ListeningService, label)

                            // 2. Save the event to the history log in the database
                            val event = DetectionEvent(
                                soundLabel = label,
                                timestamp = System.currentTimeMillis()
                            )
                            serviceScope.launch {
                                database.detectionDao().insert(event)
                            }
                        }
                    }
                    
                    // Small delay to prevent the loop from eating too much CPU
                    delay(100) 
                }
            }

        } catch (e: Exception) {
            Log.e("ListeningService", "Error starting AudioRecord: ${e.message}")
        }
    }

    private fun stopListening() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        serviceJob.cancelChildren() // Stop the coroutine loop
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        serviceJob.cancel() // Cleanup the scope
        SoundClassifier.close() // Release model resources
        Log.d("ListeningService", "Service Destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "EchoAlert Listening Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
