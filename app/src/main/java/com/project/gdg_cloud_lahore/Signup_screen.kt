package com.project.gdg_cloud_lahore

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.FirebaseDatabase
import com.project.gdg_cloud_lahore.databinding.ActivitySignupScreenBinding
import com.project.gdg_cloud_lahore.models.User

class Signup_screen : AppCompatActivity() {

    private lateinit var binding: ActivitySignupScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Register button click listener
        binding.signin.setOnClickListener {
            startActivity(Intent(this, Login_screen::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val confpass = binding.etConfirmPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if (password == confpass) {
                    // Show loading dialog while registering
                    val dialog = showLoadingDialog("Creating Account...")
                    registerUser(email, password, username, dialog)
                } else {
                    Toast.makeText(this, "Password Doesn't Match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, username: String, dialog: AlertDialog) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                dialog.dismiss() // Dismiss the dialog once registration is complete

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            // Save user data to Firebase Realtime Database
                            val userId = user.uid
                            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

                            val newUser = User(username, email, userId)
                            userRef.setValue(newUser)
                                .addOnCompleteListener { databaseTask ->
                                    if (databaseTask.isSuccessful) {
                                        Toast.makeText(this, "User registered and data saved", Toast.LENGTH_SHORT).show()

                                        // Start EmailVerificationActivity and pass the required data
                                        val intent = Intent(this, Email_Verification::class.java)
                                        intent.putExtra("username", username)
                                        intent.putExtra("email", email)
                                        intent.putExtra("password", password)
                                        startActivity(intent)

                                        // Clear input fields
                                        clearFields()
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: ${databaseTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Registration failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // Function to show loading dialog
    private fun showLoadingDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent the user from dismissing the dialog manually
            .create()

        val imageView = dialogView.findViewById<ImageView>(R.id.splashGif)

        // Load the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)  // Ensure you place splash.gif in the 'res/drawable' folder
            .into(imageView)

        dialog.show()

        return dialog
    }

    // Function to clear the input fields
    private fun clearFields() {
        binding.etEmail.text.clear()
        binding.etPassword.text.clear()
        binding.etConfirmPassword.text.clear()
        binding.username.text.clear()
    }
}
