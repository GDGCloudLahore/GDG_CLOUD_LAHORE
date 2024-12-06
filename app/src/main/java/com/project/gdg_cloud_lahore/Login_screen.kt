package com.project.gdg_cloud_lahore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.project.gdg_cloud_lahore.databinding.ActivityLoginScreenBinding
import com.project.gdg_cloud_lahore.models.User

class Login_screen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in (session exists)
        if (isUserLoggedIn()) {
            val userEmail = getUserEmailFromSession()
            if (userEmail != null) {
                Log.d("LoginScreen", "User is already logged in. Fetching user data...")
                fetchUserData(userEmail)
            } else {
                Log.d("LoginScreen", "No email found in session, staying on Login Screen.")
            }
        }

        // Google Sign-In setup
        setupGoogleSignIn()

        binding.signin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dialog = showLoadingDialog("Logging In...")
                loginUser(email, password) {
                    dialog.dismiss() // Close the dialog once the login process finishes
                    clearFields()
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, Signup_screen::class.java))
        }

        // Google Sign-In button click handler
        binding.signinGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Make sure to put the Web Client ID here
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Log the error code and message for debugging
                Log.e("LoginScreen", "Google sign-in failed with code: ${e.statusCode}")
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginScreen", "Google Sign-In successful")
                        val user = auth.currentUser
                        user?.let {
                            saveSession(it.email ?: "")
                            fetchUserData(it.email ?: "")
                        }
                    } else {
                        Log.e("LoginScreen", "Google authentication failed: ${task.exception?.message}")
                        Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun loginUser(email: String, password: String, onComplete: () -> Unit) {
        Log.d("LoginScreen", "Attempting to log in with email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                onComplete() // Ensure dialog is dismissed

                if (task.isSuccessful) {
                    Log.d("LoginScreen", "Login successful for email: $email")
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        saveSession(email) // Save session when login is successful
                        fetchUserData(email)
                    } else {
                        Log.d("LoginScreen", "Email not verified, sending verification email.")
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                        resendVerificationEmail()
                    }
                } else {
                    Log.d("LoginScreen", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                onComplete() // Ensure dialog is dismissed even in case of failure
                Log.d("LoginScreen", "Error during login: ${exception.message}")
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserData(email: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            fetchUserDataByUserId(userId)
        } else {
            Log.d("LoginScreen", "User not authenticated, redirecting to login screen.")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun fetchUserDataByUserId(userId: String) {
        FirebaseHelper.usersRef.child(userId).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue(User::class.java)
                    userData?.let {
                        Log.d("LoginScreen", "User data fetched successfully for userId: $userId")
                        // Now you can use the username field
                        val username = it.username  // Get the username
                        navigateToHomeDrive()  // You can pass username if needed
                    }
                } else {
                    Log.d("LoginScreen", "User data not found for userId: $userId")
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Log.d("LoginScreen", "Failed to retrieve user data: ${error.message}")
                Toast.makeText(this, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHomeDrive() {
        // Get the current user from Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // If the user is logged in, pass their details to the next activity
            Log.d("LoginScreen", "Navigating to Home Drive (Fragments activity).")
            val intent = Intent(this, Fragments::class.java).apply {
                putExtra("username", user.displayName)
                putExtra("user_email", user.email)
            }
            startActivity(intent)
        } else {
            // If the user is not authenticated, show an error or redirect to login
            Log.d("LoginScreen", "User not authenticated. Redirecting to login.")
            // You can redirect to the login activity here if needed
        }
    }


    private fun navigateToLogin() {
        startActivity(Intent(this, Login_screen::class.java))
        finish()
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
            if (verifyTask.isSuccessful) {
                Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSession(email: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_email", email)
            apply() // Save the email to session
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

    private fun clearFields() {
        binding.etEmail.text.clear()
        binding.etPassword.text.clear()
    }

    @SuppressLint("MissingInflatedId")
    private fun showLoadingDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val imageView = dialogView.findViewById<ImageView>(R.id.splashGif)

        Glide.with(this)
            .asGif()
            .load(R.drawable.splash)
            .into(imageView)

        dialog.show()

        return dialog
    }

    object FirebaseHelper {
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    }
}
