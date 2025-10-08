package com.example.thai_dessert_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Check if user already logged in ---
        val currentUserPref = getSharedPreferences("CurrentUserPrefs", Context.MODE_PRIVATE)
        val savedEmail = currentUserPref.getString("currentUserEmail", null)
        if (savedEmail != null) {
            // Already logged in -> go to AccountPage
            val intent = Intent(this, AccountPage::class.java)
            intent.putExtra("email", savedEmail)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login_page)

        // back button
        val buttonBack = findViewById<Button>(R.id.button_back)

        buttonBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerButton: Button = findViewById(R.id.buttonRegister)

        // SharedPreferences to store users locally
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // --- Login button ---
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPassword = sharedPref.getString(email, null)
            if (savedPassword != null && savedPassword == password) {
                // Save currently logged-in user
                currentUserPref.edit().putString("currentUserEmail", email).apply()

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AccountPage::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Register button ---
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user in SharedPreferences
            sharedPref.edit().putString(email, password).apply()

            // Save currently logged-in user
            currentUserPref.edit().putString("currentUserEmail", email).apply()

            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

            // Automatically go to AccountPage
            val intent = Intent(this, AccountPage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
            finish()
        }
    }
}
