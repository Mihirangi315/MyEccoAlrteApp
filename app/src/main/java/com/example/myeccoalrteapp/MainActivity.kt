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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private var isListening = false
    private var pulseAnimator: ObjectAnimator? = null

    /**
     * Helper to request permissions. 
     * RECORD_AUDIO is needed for the microphone.
     * POST_NOTIFICATIONS is needed for the Foreground Service on Android 13+.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            // Permission granted, now we can start the service
            toggleListening()
        } else {
            Toast.makeText(this, "Microphone permission is required for EchoAlert", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHistoryList()
        setupButtons()
    }

    private fun setupHistoryList() {
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        
        // Updated dummy data with appropriate icons
        val dummyData = listOf(
            DetectionEvent("Doorbell", "10:15 AM", R.drawable.ic_doorbell),
            DetectionEvent("Alarm", "09:45 AM", R.drawable.ic_alarm),
            DetectionEvent("Knock", "08:30 AM", R.drawable.ic_knock),
            DetectionEvent("Baby Crying", "07:15 AM", R.drawable.ic_baby),
            DetectionEvent("Phone Ringing", "06:00 AM", R.drawable.ic_phone_ring)
        )

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = DetectionAdapter(dummyData)
    }

    private fun setupButtons() {
        val btnToggle = findViewById<MaterialButton>(R.id.btnToggleListening)
        val btnSettings = findViewById<View>(R.id.btnSettings)

        btnToggle.setOnClickListener {
            checkPermissionsAndToggle()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * Checks if we have the necessary permissions before toggling the service.
     */
    private fun checkPermissionsAndToggle() {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        
        // Android 13 (API 33) and above requires explicit permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            toggleListening()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Logic to switch between listening and idle states.
     */
    private fun toggleListening() {
        val btnToggle = findViewById<MaterialButton>(R.id.btnToggleListening)
        val cvStatus = findViewById<MaterialCardView>(R.id.cvStatus)
        val tvStatusText = findViewById<TextView>(R.id.tvStatusText)

        if (isListening) {
            stopListeningService()
            btnToggle.text = getString(R.string.start_listening)
            tvStatusText.text = getString(R.string.status_ready)
            cvStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_idle))
            stopPulseAnimation()
        } else {
            startListeningService()
            btnToggle.text = getString(R.string.stop_listening)
            tvStatusText.text = getString(R.string.status_listening_active)
            cvStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_listening))
            startPulseAnimation(cvStatus)
        }
        isListening = !isListening
    }

    /**
     * Creates a simple pulsing effect by animating the scale of the Status Card.
     */
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

    private fun startListeningService() {
        startService(Intent(this, ListeningService::class.java))
    }

    private fun stopListeningService() {
        stopService(Intent(this, ListeningService::class.java))
    }
}
