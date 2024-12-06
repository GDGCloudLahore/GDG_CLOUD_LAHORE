package com.project.gdg_cloud_lahore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.project.gdg_cloud_lahore.databinding.ActivityFragmentsBinding
import com.project.gdg_cloud_lahore.fragments.Home_Drive
import com.project.gdg_cloud_lahore.fragments.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView

class Fragments : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user data from the Intent
        val userName = intent.getStringExtra("user_name")
        val userEmail = intent.getStringExtra("user_email")

        // Pass the data to the fragments if they haven't been recreated
        if (savedInstanceState == null) {
            val homeDriveFragment = Home_Drive().apply {
                arguments = Bundle().apply {
                    putString("user_name", userName)
                    putString("user_email", userEmail)
                }
            }
            openFragment(homeDriveFragment)
            updateMenuIcons(R.id.nav_home)
        }

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val homeDriveFragment = Home_Drive().apply {
                        arguments = Bundle().apply {
                            putString("username", userName)
                            putString("user_email", userEmail)
                        }
                    }
                    openFragment(homeDriveFragment)
                    updateMenuIcons(R.id.nav_home)
                    true
                }
                R.id.nav_profile -> {
                    val profileFragment = Profile().apply {
                        arguments = Bundle().apply {
                            putString("username", userName)
                            putString("user_email", userEmail)
                        }
                    }
                    openFragment(profileFragment)
                    updateMenuIcons(R.id.nav_profile)
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

    private fun updateMenuIcons(selectedItemId: Int) {
        val menu = binding.bottomNav.menu
        menu.findItem(R.id.nav_home).setIcon(R.drawable.ic_home)
        menu.findItem(R.id.nav_profile).setIcon(R.drawable.ic_user)

        when (selectedItemId) {
            R.id.nav_home -> menu.findItem(R.id.nav_home).setIcon(R.drawable.ic_home_selected)
            R.id.nav_profile -> menu.findItem(R.id.nav_profile).setIcon(R.drawable.ic_profile_selected)
        }
    }
}
