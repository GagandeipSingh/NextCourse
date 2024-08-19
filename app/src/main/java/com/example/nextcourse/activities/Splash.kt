package com.example.nextcourse.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivitySplashBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add this line to disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showContentWithAnimation()
        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        val auth  = Firebase.auth
        if(auth.currentUser != null){
            binding.btnStart.setOnClickListener {
                startActivity(Intent(this, ClassesView::class.java))
                finish()
            }
        }
    }

    private fun showContentWithAnimation() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1600L // Adjust duration for desired animation speed
            interpolator = AccelerateDecelerateInterpolator() // Adjust interpolator for animation style
        }
        // Start the animation on the view
        binding.animationView.startAnimation(fadeIn)
        binding.animationView.visibility = View.VISIBLE
        binding.btnStart.startAnimation(fadeIn)
        binding.btnStart.visibility = View.VISIBLE
    }
}