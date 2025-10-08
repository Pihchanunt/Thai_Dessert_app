package com.example.thai_dessert_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import android.os.Handler
import android.os.Looper
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed ({
            val intent = Intent(this@MainActivity, HomePage::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator, android.R.anim.accelerate_decelerate_interpolator)
            finish()
        },1500 )
    }
}