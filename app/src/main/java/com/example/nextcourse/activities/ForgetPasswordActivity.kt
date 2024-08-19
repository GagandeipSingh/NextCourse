package com.example.nextcourse.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityForgetPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ForgetPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var auth : FirebaseAuth
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add this line to disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val rootView = window.decorView.rootView
        rootView.setOnApplyWindowInsetsListener { _, windowInsets ->
            val imeVisible = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom > 0
            if (imeVisible) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 640)
                }
            } else {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 0)
                }
            }
            windowInsets
        }

        binding.btnForgotPasswordSubmit.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword(){
        val email = binding.etForgotPasswordEmail.text.toString()
        if(validateForm(email)){
            showProgressBar()
            auth.sendPasswordResetEmail(email).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    dismissProgessBar()
                    binding.etForgotPasswordEmail.visibility = View.GONE
                    binding.tvSubmitMsg.visibility = View.VISIBLE
                    binding.tilEmailForgetPassword.visibility = View.GONE
                    binding.btnForgotPasswordSubmit.visibility = View.GONE
                }
                else{
                    dismissProgessBar()
                    showToast(this,"Cannot Reset your Password.. Try again later..")
                }
            }
        }
    }
    private fun validateForm(email:String):Boolean
    {
        return when {
            TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.etForgotPasswordEmail.error = "Enter valid email address"
                false
            }
            else -> { true }
        }
    }
}