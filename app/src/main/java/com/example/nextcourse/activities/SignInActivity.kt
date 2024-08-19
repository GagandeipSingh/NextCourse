package com.example.nextcourse.activities
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.R
import com.example.nextcourse.domains.UserDomain
import com.example.nextcourse.databinding.ActivitySignInBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class SignInActivity : BaseActivity() {
    private lateinit var binding:ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseCredential:AuthCredential
    private lateinit var name:String
    private lateinit var email:String
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
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
        binding.etSinInEmail.doOnTextChanged{_,_,_,_ ->
            binding.tilEmail.isErrorEnabled = false
        }
        binding.etSinInPassword.doOnTextChanged{_,_,_,_ ->
            binding.tilPassword.isErrorEnabled = false
        }
        binding.tvRegister.setOnClickListener{
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, 0)
            }
            binding.etSinInEmail.text = null
            binding.etSinInPassword.text = null
            binding.etSinInPassword.clearFocus()
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }, 200)

        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this@SignInActivity, ForgetPasswordActivity::class.java))
        }
        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
        binding.btnSignInWithGoogle.setOnClickListener {
            showProgressBar()
            signIn(this)
        }
    }
    private fun signInUser(){
        val email = binding.etSinInEmail.text.toString()
        val password = binding.etSinInPassword.text.toString()
        if(validateForm(email,password)){
            showProgressBar()
            firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        startActivity(Intent(this, ClassesView::class.java))
                        finish()
                        dismissProgessBar()
                    }
                    else{
                        Toast.makeText(this,"Check Email & Password Again..",Toast.LENGTH_SHORT).show()
                        dismissProgessBar()
                    }
                }
        }
    }
    private fun validateForm(email:String,password:String):Boolean
    {
        return when {
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
    private fun signIn(context: SignInActivity) {
        val credentialManager = CredentialManager.create(this)
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setNonce(hashedNonce)
            .build()
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                // Use the ID token to authenticate with Firebase
                firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this@SignInActivity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success
                            getDetails()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (@SuppressLint("NewApi", "LocalSuppress") e: GetCredentialException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.d("Error: ",e.message.toString())
                dismissProgessBar()
            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                Log.d("Error: ",e.message.toString())
                dismissProgessBar()
            } catch (e: CancellationException) {
                // Handle cancellation
                Toast.makeText(context, "Account selection was cancelled", Toast.LENGTH_SHORT).show()
                Log.d("Error: ",e.message.toString())
                dismissProgessBar()
            }catch (e: GetCredentialCancellationException) {
                // Handle the exception here
                Toast.makeText(context, "Account selection was cancelled", Toast.LENGTH_SHORT).show()
                Log.d("Error: ",e.message.toString())
                dismissProgessBar()
            }catch (e: NoCredentialException) {
                Toast.makeText(context, "Too many sign-in attempts. Please try again later.", Toast.LENGTH_SHORT).show()
                Log.d("Error: ",e.message.toString())
                dismissProgessBar()
            }
        }
    }

    private fun getDetails(){
        val user = firebaseAuth.currentUser
        user?.let {
            name = it.displayName.toString()
            email = it.email.toString()
//            Log.d("SignInActivity", "Name: $name, Email: $email")
            val database = Firebase.database
            val myRef = database.getReference("Users").child(user.uid)
            myRef.setValue(UserDomain(name,email)).addOnSuccessListener {
                startActivity(Intent(this, ClassesView::class.java))
                finish()
            }
        }
    }
}
