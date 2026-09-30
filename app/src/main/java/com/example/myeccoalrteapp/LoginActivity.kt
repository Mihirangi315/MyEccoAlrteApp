package com.example.myeccoalrteapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        applySavedTheme()
        
        val prefs = getSharedPreferences("EchoAlertPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoToSignup = findViewById<MaterialButton>(R.id.btnGoToSignup)

        btnLogin.setOnClickListener {
            val userInput = etUsername.text.toString().trim()
            val passInput = etPassword.text.toString()

            val storedUser = prefs.getString("stored_user", null)
            val storedPass = prefs.getString("stored_pass", null)

            // If no user exists yet, allow any login (for convenience) or force signup
            if (storedUser == null) {
                if (userInput.isNotEmpty() && passInput.isNotEmpty()) {
                    performLogin(userInput)
                } else {
                    Toast.makeText(this, "Please sign up first", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Check against stored credentials
                if (userInput == storedUser && passInput == storedPass) {
                    performLogin(userInput)
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun performLogin(username: String) {
        val prefs = getSharedPreferences("EchoAlertPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_logged_in", true).apply()
        Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences("EchoAlertPrefs", Context.MODE_PRIVATE)
        val themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
}
