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

class ListeningService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private lateinit var database: AppDatabase

    // --- NITRO SPEED SETTINGS ---
    private val sampleRate = 16000 
    private val classificationBufferSize = 15600
    private val stepSize = 1600 // Check every 100ms (10 times a second!)
    
    // Cache settings in memory for instant access
    private var cachedSensitivity = 0.10f
    private var cachedEnabledSounds = mutableSetOf<String>()
    private var cachedRingtone = false
    private var cachedContinuous = true
    private var lastAlertTime = 0L

    companion object {
        private const val CHANNEL_ID = "EchoAlertChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_DISMISS_ALERT = "ACTION_DISMISS_ALERT"

        // Static flag to let the UI know if we are currently listening
        var isRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        SoundClassifier.initialize(this)
        database = AppDatabase.getDatabase(this)
        
        // Load settings into cache immediately
        refreshSettingsCache()
    }

    private fun refreshSettingsCache() {
        serviceScope.launch {
            val settings = database.soundSettingDao().getAllSettings()
            cachedEnabledSounds.clear()
            settings.forEach {
                if (it.soundLabel == "GlobalSensitivity") cachedSensitivity = it.sensitivity
                else if (it.soundLabel == "RingtoneAlert") cachedRingtone = it.isEnabled
                else if (it.soundLabel == "ContinuousAlert") cachedContinuous = it.isEnabled
                else if (it.isEnabled) cachedEnabledSounds.add(it.soundLabel)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISMISS_ALERT) {
            AlertManager.stopVibration()
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(1001)
            return START_STICKY
        }
        
        refreshSettingsCache() // Refresh cache whenever service is interacted with
        Log.d("ListeningService", "Service Started (Nitro Mode)")
        startAsForeground()
        startListening()
        return START_STICKY
    }

    private fun startAsForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EchoAlert Active")
            .setContentText("Listening 10x per second for sounds...")
            .setSmallIcon(R.drawable.ic_listening_active)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startListening() {
        if (isRecording) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2)
            audioRecord?.startRecording()
            isRecording = true

            serviceScope.launch {
                val slidingBuffer = ShortArray(classificationBufferSize)
                val floatBuffer = FloatArray(classificationBufferSize)
                val stepBuffer = ShortArray(stepSize)

                while (isActive && isRecording) {
                    var read = 0
                    while (read < stepSize && isActive && isRecording) {
                        val result = audioRecord?.read(stepBuffer, read, stepSize - read) ?: -1
                        if (result > 0) read += result else break
                    }

                    if (read == stepSize) {
                        System.arraycopy(slidingBuffer, stepSize, slidingBuffer, 0, classificationBufferSize - stepSize)
                        System.arraycopy(stepBuffer, 0, slidingBuffer, classificationBufferSize - stepSize, stepSize)

                        for (i in 0 until classificationBufferSize) {
                            floatBuffer[i] = slidingBuffer[i] / 32768.0f
                        }

                        val result = SoundClassifier.classify(floatBuffer)
                        result?.let { (label, confidence) ->
                            handleDetection(label, confidence)
                        }
                    }
                }
            }
        } catch (e: Exception) { Log.e("ListeningService", "Error: ${e.message}") }
    }

    private fun handleDetection(label: String, confidence: Float) {
        // Instant check using cache (No database delay!)
        if (cachedEnabledSounds.contains(label) && confidence >= cachedSensitivity) {
            
            // Prevent "Double Alerts" within 2 seconds
            if (System.currentTimeMillis() - lastAlertTime < 2000) return
            lastAlertTime = System.currentTimeMillis()

            Log.i("ListeningService", "TRIGGER! $label ($confidence)")
            AlertManager.trigger(this, label, cachedRingtone, cachedContinuous)

            // Save to DB in background
            serviceScope.launch {
                database.detectionDao().insert(DetectionEvent(soundLabel = label, timestamp = System.currentTimeMillis()))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        AlertManager.stopVibration()
        serviceJob.cancel()
        SoundClassifier.close()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "EchoAlert Service", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
        }
    }
}
