package com.project.gdg_cloud_lahore.fragments

import android.app.AlertDialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.project.gdg_cloud_lahore.R
import com.google.firebase.auth.FirebaseAuth

class Profile : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements

        userEmailTextView = binding.findViewById(R.id.userEmail)
        logoutButton = binding.findViewById(R.id.logoutButton)

        // Retrieve user details passed from Login_screen
        val userName = arguments?.getString("username")
        val userEmail = arguments?.getString("user_email")

        // Display the user's name and email

        userEmailTextView.text = userEmail ?: "No Email"

        // Set up the Logout button
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return binding
    }

    // Show confirmation dialog before logging out
    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    // Logout user
    private fun logoutUser() {
        auth.signOut() // Sign out from Firebase
        // Navigate back to the login screen (or call any other desired behavior)
        requireActivity().finish() // Close the current activity (logout)
    }
}
