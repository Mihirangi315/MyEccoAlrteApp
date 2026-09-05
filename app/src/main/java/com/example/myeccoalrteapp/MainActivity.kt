package com.example.myeccoalrteapp

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

/**
 * MainActivity is the "Conductor" of the EchoAlert app.
 * It connects all 5 modules: UI, Audio Service, ML, Data (Room), and Alerts.
 */
class MainActivity : AppCompatActivity() {

    // --- Module 1: UI State ---
    private var isListening = false
    private var pulseAnimator: ObjectAnimator? = null
    private lateinit var database: AppDatabase

    /**
     * Permission Launcher (Module 2: Audio & Alerts)
     * We need RECORD_AUDIO to listen and POST_NOTIFICATIONS to alert the user.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            // If the user said yes, we can start the service!
            toggleListening()
        } else {
            Toast.makeText(this, "Microphone permission is required!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- Module 4: Data (Room) ---
        // Get the single instance of our database
        database = AppDatabase.getDatabase(this)
        initializeDefaultSettings()

        // UI Setup for modern Android (Edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHistoryList()
        setupButtons()
    }

    /**
     * --- Module 1 & 4 Integration: History List ---
     * This connects our UI (RecyclerView) to our Data (Room Database).
     */
    private fun setupHistoryList() {
        val recyclerHistory = findViewById<RecyclerView>(R.id.recyclerHistory)
        recyclerHistory.layoutManager = LinearLayoutManager(this)

        /**
         * OBSERVER APPROACH: This is the best way to handle "refreshes".
         * Instead of manually refreshing the list, we "observe" the database.
         * Whenever the Audio Service (Module 2) saves a new sound, Room 
         * automatically notifies this block, and the UI updates instantly!
         */
        lifecycleScope.launch {
            database.detectionDao().getAllDetections().collect { detections ->
                // This code runs every time a new sound is detected and saved!
                recyclerHistory.adapter = HistoryAdapter(detections)
            }
        }
    }

    /**
     * --- Module 1 & 2 Integration: Control ---
     * This connects the Toggle Button to the Audio Capture Service.
     */
    private fun setupButtons() {
        val btnToggle = findViewById<MaterialButton>(R.id.btnToggleListening)
        val btnSettings = findViewById<View>(R.id.btnSettings)

        // When the user clicks the big button...
        btnToggle.setOnClickListener {
            // First, check if we have permission to use the Mic
            checkPermissionsAndToggle()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkPermissionsAndToggle() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            toggleListening()
        } else {
            // Ask the user for permission if we don't have it yet
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    /**
     * Starts or Stops the Foreground Service (Module 2).
     */
    private fun toggleListening() {
        val btnToggle = findViewById<MaterialButton>(R.id.btnToggleListening)
        val cvStatus = findViewById<MaterialCardView>(R.id.cvStatus)
        val tvStatusText = findViewById<TextView>(R.id.tvStatusText)

        if (isListening) {
            // STOP Module 2
            stopService(Intent(this, ListeningService::class.java))
            
            // Update UI
            btnToggle.text = getString(R.string.start_listening)
            tvStatusText.text = getString(R.string.status_ready)
            cvStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_idle))
            stopPulseAnimation()
        } else {
            // START Module 2 (Audio Capture + ML + Alerts)
            startService(Intent(this, ListeningService::class.java))
            
            // Update UI
            btnToggle.text = getString(R.string.stop_listening)
            tvStatusText.text = getString(R.string.status_listening_active)
            cvStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_listening))
            startPulseAnimation(cvStatus)
        }
        isListening = !isListening
    }

    // --- UI Polish: Pulsing Animation ---
    private fun startPulseAnimation(view: View) {
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.05f),
            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.05f)
        ).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        findViewById<View>(R.id.cvStatus).apply {
            scaleX = 1.0f
            scaleY = 1.0f
        }
    }

    private fun initializeDefaultSettings() {
        lifecycleScope.launch {
            val currentSettings = database.soundSettingDao().getAllSettings()
            if (currentSettings.isEmpty()) {
                val defaults = listOf(
                    SoundSetting("Doorbell", true, 0.10f),
                    SoundSetting("Alarm", true, 0.10f),
                    SoundSetting("Knock", true, 0.10f),
                    SoundSetting("Baby crying", true, 0.10f),
                    SoundSetting("Telephone bell ringing", true, 0.10f),
                    SoundSetting("GlobalSensitivity", true, 0.10f), // Easy start
                    SoundSetting("RingtoneAlert", true, 0.0f),      // ON by default
                    SoundSetting("ContinuousAlert", true, 0.0f)     // ON by default
                )
                defaults.forEach { database.soundSettingDao().saveSetting(it) }
            }
        }
    }
}
