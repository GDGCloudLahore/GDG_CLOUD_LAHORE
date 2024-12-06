package com.project.gdg_cloud_lahore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.project.gdg_cloud_lahore.databinding.ActivityMainBinding
import com.project.gdg_cloud_lahore.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Load the splash.gif into the ImageView
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash) // Use splash.gif located in the res/drawable folder
            .into(binding.splashGif)

        // Check if the user is already logged in (session exists)
        if (isUserLoggedIn()) {
            val userEmail = getUserEmailFromSession()
            if (userEmail != null) {
                Log.d("MainActivity", "User is already logged in. Fetching user data...")
                fetchUserData(userEmail)
            } else {
                Log.d("MainActivity", "No email found in session, staying on Login Screen.")
            }
        } else {
            // Delay for 3 seconds and navigate to Login_Screen
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToLogin()
            }, 3000) // 3000ms = 3 seconds
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.contains("user_email") // Check if session exists
    }

    private fun getUserEmailFromSession(): String? {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("user_email", null) // Get email from session
    }

    private fun fetchUserData(email: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchUserDataByUserId(userId)
        } else {
            Log.d("MainActivity", "User not authenticated, redirecting to login screen.")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun fetchUserDataByUserId(userId: String) {
        Login_screen.FirebaseHelper.usersRef.child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue(User::class.java)
                    userData?.let {
                        Log.d("MainActivity", "User data fetched successfully for userId: $userId")
                        // Proceed to the home screen
                        navigateToHomeDrive()  // You can pass username if needed
                    }
                } else {
                    Log.d("MainActivity", "User data not found for userId: $userId")
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            }
            .addOnFailureListener { error ->
                Log.d("MainActivity", "Failed to retrieve user data: ${error.message}")
                Toast.makeText(this, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
    }

    private fun navigateToHomeDrive() {
        // Get the current user from Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // If the user is logged in, pass their details to the next activity
            Log.d("MainActivity", "Navigating to Home Drive (Fragments activity).")
            val intent = Intent(this, Fragments::class.java).apply {
                putExtra("username", user.displayName)
                putExtra("user_email", user.email)
            }
            startActivity(intent)
            finish() // Prevent user from going back to the splash screen
        } else {

            Log.d("MainActivity", "User not authenticated. Redirecting to login.")
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, Login_screen::class.java))
        finish() // Finish MainActivity so it won't reappear on back press
    }
}
