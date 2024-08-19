package com.example.nextcourse.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.domains.ClassDomain1
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityJoinClassBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JoinClassActivity : BaseActivity() {
    private lateinit var binding: ActivityJoinClassBinding
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userId: String
    private lateinit var creator : String
    private lateinit var key :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityJoinClassBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        intent.extras.let {
            userName = it?.getString("Name").toString()
            userEmail = it?.getString("Email").toString()
            userId = it?.getString("UserId").toString()
        }
        binding.userName.text = userName
        binding.userEmail.text = userEmail
        binding.cancel.setOnClickListener {
            finish()
        }
        binding.joinBtn.setOnClickListener {
            binding.etCodeText.clearFocus()
            joinClass()
        }
    }

    private fun joinClass() {
        val classId = binding.etCodeText.text.toString()
        if (validateCode(classId)) {
            searchClassId(classId)
        }
    }
    private fun searchClassId(classId: String) {
        binding.etCodeText.text = null
        val databaseReference = FirebaseDatabase.getInstance().getReference("Classes")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isClassIdFound = false
                    val classesSnapshot = snapshot.children
                    for (classIdSnapshot in classesSnapshot) {
                        val classData = classIdSnapshot.getValue(ClassDomain1::class.java)
                        classData?.let {
                            creator = it.creator
                            key = classIdSnapshot.key.toString()
                        }
                        if(userId == creator) break // Skip the current user's classes
                        if (key == classId) {
                            classIdSnapshot.ref.child("Students").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(studentSnapshot: DataSnapshot) {
                                    if (studentSnapshot.exists()) {
                                        Toast.makeText(this@JoinClassActivity,"Already Joined..",Toast.LENGTH_SHORT).show()
                                    } else {
                                        classIdSnapshot.ref.child("Students").child(userId).setValue(userId) // Add the current user's ID to the 'Students' subnode of the found class ID
                                        Toast.makeText(this@JoinClassActivity,"Successfully Added..",Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })
                            isClassIdFound = true
                            break
                        }
                    }

                if (!isClassIdFound) {
                    Toast.makeText(this@JoinClassActivity,"Class Not Found",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }


    private fun validateCode(classCode: String): Boolean {
        var isValid = true
        if (TextUtils.isEmpty(classCode)) {
            binding.tilCode.error = "Enter Class Code"
            isValid = false
        } else {
            binding.tilCode.error = null
        }
        return isValid
    }
}