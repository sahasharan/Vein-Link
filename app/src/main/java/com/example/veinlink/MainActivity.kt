package com.example.veinlink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// Assuming RegisterActivity is in com.example.veinlink.auth package
import com.example.veinlink.auth.RegisterActivity

class MainActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // This is our splash screen layout

        // Handler to navigate to RegisterActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Finish MainActivity so the user can't navigate back to it
        }, SPLASH_DELAY)
    }
}