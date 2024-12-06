package com.project.gdg_cloud_lahore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Email_Verification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)  // Ensure this is correct

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        val btnGoToLogin: Button = findViewById(R.id.btn_chk)
        val tvMessage: TextView = findViewById(R.id.tv_message)

        // Set text for message
        tvMessage.text = "Please verify your email before logging in."

        // Button Click Listener for going to Login screen
        btnGoToLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        // Navigate to the Login screen
        val intent = Intent(this, Login_screen::class.java)  // Replace LoginActivity with your actual login activity
        startActivity(intent)
        finish()  // Close the current screen
    }
}
