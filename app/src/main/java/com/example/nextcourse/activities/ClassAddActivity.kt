package com.example.nextcourse.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.domains.ClassDomain1
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityClassAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ClassAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassAddBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var classTitle:String
    private lateinit var subject:String
    private lateinit var section:String
    private lateinit var userId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add this line to disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityClassAddBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)
        val scrollView = binding.scrollView

        val sectionFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, v.bottom + binding.scrollView.paddingBottom)
                }
            }
        }

        val subjectFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, v.bottom + binding.scrollView.paddingBottom)
                }
            }
        }

        binding.section.onFocusChangeListener = sectionFocusListener
        binding.subject.onFocusChangeListener = subjectFocusListener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.saveButton.setOnClickListener {
            createClass()
        }
    }

    private fun createClass() {
        classTitle = binding.classTitle.text.toString()
        section = binding.section.text.toString()
        subject = binding.subject.text.toString()
        userId = auth.currentUser?.uid.toString()
        val classVar = ClassDomain1(classTitle,section,subject,userId)
        if(validateClass(classTitle,section,subject)){
            val database = Firebase.database
            val ref = database.getReference("Classes")
            val classRef = ref.push()
            classRef.setValue(classVar).addOnSuccessListener {
                Toast.makeText(this,"Class Added..", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ClassAddActivity, ClassesView::class.java))
                finish()
                }
        }
        }

    private fun validateClass(classTitle: String, section: String, subject: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(classTitle)) {
            binding.tilClassTitle.error = "Enter Class Name"
            isValid = false
        } else {
            binding.tilClassTitle.error = null
        }

        if (TextUtils.isEmpty(section)) {
            binding.tilSection.error = "Enter Section"
            isValid = false
        } else {
            binding.tilSection.error = null
        }

        if (TextUtils.isEmpty(subject)) {
            binding.tilSubject.error = "Enter Subject"
            isValid = false
        } else {
            binding.tilSubject.error = null
        }

        return isValid
    }

}