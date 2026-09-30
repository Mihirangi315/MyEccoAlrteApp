package com.example.myeccoalrteapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<TextInputEditText>(R.id.etSignupUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etSignupPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etSignupConfirmPassword)
        val btnSignup = findViewById<MaterialButton>(R.id.btnDoSignup)
        val btnBack = findViewById<MaterialButton>(R.id.btnBackToLogin)

        btnSignup.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save credentials locally
            val prefs = getSharedPreferences("EchoAlertPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("stored_user", user)
                .putString("stored_pass", pass)
                .apply()

            Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_LONG).show()
            finish()
        }

        btnBack.setOnClickListener { finish() }
    }
}
