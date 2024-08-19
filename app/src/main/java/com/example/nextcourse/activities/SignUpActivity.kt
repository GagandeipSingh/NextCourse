package com.example.nextcourse.activities
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.R
import com.example.nextcourse.domains.UserDomain
import com.example.nextcourse.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {
        private lateinit var binding: ActivitySignUpBinding
        private lateinit var auth: FirebaseAuth
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Add this line to disable dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding = ActivitySignUpBinding.inflate(layoutInflater)
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
        binding.etSinUpName.doOnTextChanged{_,_,_,_ ->
            binding.tilName.isErrorEnabled = false
        }
        binding.etSinUpEmail.doOnTextChanged{_,_,_,_ ->
            binding.tilEmail.isErrorEnabled = false
        }
        binding.etSinUpPassword.doOnTextChanged{_,_,_,_ ->
            binding.tilPassword.isErrorEnabled = false
        }
            binding.tvLoginPage.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finish()
            }
            binding.btnSignUp.setOnClickListener {
                registerUser()
            }
        }

    private fun registerUser()
    {
        val name = binding.etSinUpName.text.toString()
        val email = binding.etSinUpEmail.text.toString()
        val password = binding.etSinUpPassword.text.toString()
        val userDomainData = UserDomain(name,email)
        if (validateForm(name, email, password))
        {
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful)
                    {
                        val database = Firebase.database
                        val myRef = database.getReference("Users").child(auth.currentUser!!.uid)
                        myRef.setValue(userDomainData).addOnSuccessListener {
                            Toast.makeText(this,"Successful.. Login Now..", Toast.LENGTH_SHORT).show()
                            dismissProgessBar()
                            startActivity(Intent(this, SignInActivity::class.java))
                            finish()
                        }
                    }
                    else {
                        val exception = task.exception
                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                Toast.makeText(this, "The password is too weak.", Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(this, "Email is already in use.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dismissProgessBar()
                    }
                }
        }
    }
    private fun validateForm(name:String, email:String,password:String):Boolean
    {
        return when {
            TextUtils.isEmpty(name)->{
                binding.tilName.error = "Enter name"
                false
            }
            TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()->{
                binding.tilEmail.error = "Enter valid email address"
                false
            }
            TextUtils.isEmpty(password)->{
                binding.tilPassword.error = "Enter password"
                false
            }
            else -> { true }
        }
    }
}