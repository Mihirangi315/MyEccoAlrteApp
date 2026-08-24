package com.example.myeccoalrteapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Find the back button and finish the activity when clicked
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // finish() closes the current screen and takes you back to the previous one
            finish()
        }
    }
}
