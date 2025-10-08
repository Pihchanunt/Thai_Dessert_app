package com.example.thai_dessert_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val accountButton: ImageButton = findViewById(R.id.account_icon)
        val sharedPref = getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)

        accountButton.setOnClickListener {
            val currentUserEmail = sharedPref.getString("currentUserEmail", null)
            if (currentUserEmail.isNullOrEmpty()) {
                // No user logged in → go to LoginPage
                val intent = Intent(this, LoginPage::class.java)
                startActivity(intent)
            } else {
                // User logged in → go to AccountPage
                val intent = Intent(this, AccountPage::class.java)
                intent.putExtra("email", currentUserEmail)
                startActivity(intent)
            }
        }


        // .tumkhanom icon button
        val buttonIcon = findViewById<ImageButton>(R.id.clickable_icon)
        buttonIcon.setOnClickListener {
            val intentHome = Intent(this, HomePage::class.java)
            startActivity(intentHome)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Show all button
        val buttonShowall = findViewById<Button>(R.id.button_show_all)
        buttonShowall.setOnClickListener {
            val intentShowall = Intent(this, ListPage::class.java)
            intentShowall.putExtra("SelectedRegion","All")
            startActivity(intentShowall)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        // North button
        val buttonNorth = findViewById<Button>(R.id.button_north)
        buttonNorth.setOnClickListener {
            val intentNorth = Intent(this, ListPage::class.java)
            intentNorth.putExtra("selectedRegion", "North")
            startActivity(intentNorth)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }
        // Northeast button
        val buttonNortheast = findViewById<Button>(R.id.button_northeast)
        buttonNortheast.setOnClickListener {
            val intentNortheast = Intent(this, ListPage::class.java)
            intentNortheast.putExtra("selectedRegion", "Northeast")
            startActivity(intentNortheast)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        // Central button
        val buttonCentral = findViewById<Button>(R.id.button_central)
        buttonCentral.setOnClickListener {
            val intentCentral = Intent(this, ListPage::class.java)
            intentCentral.putExtra("selectedRegion", "Central")
            startActivity(intentCentral)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        // South button
        val buttonSouth = findViewById<Button>(R.id.button_south)
        buttonSouth.setOnClickListener {
            val intentSouth = Intent(this, ListPage::class.java)
            intentSouth.putExtra("selectedRegion", "South")
            startActivity(intentSouth)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

    }
}