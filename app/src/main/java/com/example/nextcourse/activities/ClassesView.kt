package com.example.nextcourse.activities

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nextcourse.R
import com.example.nextcourse.adapters.ClassAdapter
import com.example.nextcourse.databinding.ActivityClassesViewBinding
import com.example.nextcourse.domains.ClassDomain1
import com.example.nextcourse.domains.ClassDomain2
import com.example.nextcourse.domains.UserDomain
import com.example.nextcourse.progress.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ClassesView : BaseActivity() {
    private lateinit var binding: ActivityClassesViewBinding
    private lateinit var classAdapter: ClassAdapter
    private lateinit var dataList: ArrayList<ClassDomain2>
    private lateinit var auth : FirebaseAuth
    private lateinit var bookBgDrawables : Array<Int>
    private lateinit var userName : String
    private lateinit var userEmail : String
    private lateinit var userId : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityClassesViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        auth = FirebaseAuth.getInstance()
        dataList = ArrayList()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showProgressBar()
        // Fetch the username from Firebase
        userId = auth.currentUser?.uid?:""
        val database = Firebase.database
        val myRef = database.getReference("Users").child(userId)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                userName = userDomainData?.name.toString()
                userEmail = userDomainData?.email.toString()
                if (userName != "") {
                    binding.welcome.text = getString(R.string.welcome_user, userName)

                } else {
                    binding.welcome.text = getString(R.string.welcome_anonymous)
                    Log.e("ClassesActivity", "UserDomain name is null")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ClassesActivity", "Failed to get user name", databaseError.toException())
            }
        })

        binding.btnSignOut.setOnClickListener {
            if(auth.currentUser != null){
                auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
        bookBgDrawables = arrayOf(
            R.drawable.book_bg1,
            R.drawable.book_bg2,
            R.drawable.book_bg3,
            R.drawable.book_bg4,
            R.drawable.book_bg5,
            R.drawable.book_bg6
        )
        retrieveClasses()

        classAdapter = ClassAdapter(dataList,this)
        binding.ClassesView.layoutManager = LinearLayoutManager(this)
        binding.ClassesView.adapter = classAdapter

        binding.addFloat.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.add_dialog_layout)

            // Set dialog window attributes for full width and bottom positioning
            val window = dialog.window
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.attributes?.gravity = Gravity.BOTTOM

            // Set background drawable for customization
            window?.setBackgroundDrawable(AppCompatResources.getDrawable(this,
                R.drawable.add_dialog_bg
            ))
            val joinDialog = dialog.findViewById<TextView>(R.id.joinDialog)
            joinDialog.setOnClickListener {
                val intent = Intent(this@ClassesView, JoinClassActivity::class.java)
                intent.putExtra("Name",userName)
                intent.putExtra("Email",userEmail)
                intent.putExtra("UserId",userId)
                startActivity(intent)
                dialog.dismiss()

            }

            val createDialog = dialog.findViewById<TextView>(R.id.createDialog)
            createDialog.setOnClickListener {
                startActivity(Intent(this@ClassesView, ClassAddActivity::class.java))
                dialog.dismiss()
            }

            dialog.show()
        }

    }

    private fun retrieveClasses() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val classesRef = database.getReference("Classes")
            val dataList = mutableListOf<ClassDomain2>()
            var count = 0

            classesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear()

                        for (classSnapshot in dataSnapshot.children) {
                            val classData = classSnapshot.getValue(ClassDomain1::class.java)
                            classData?.let {
                                val classTitle = it.classTitle
                                val section = it.section
                                val subject = it.subject
                                val creator = it.creator
                                val key = classSnapshot.key.toString()
                                val pic2 = bookBgDrawables[count % 6]
                                count++

                                // Check if the user is the creator or a student of the class
                                if (creator == userId ) {
                                    dataList.add(ClassDomain2(classTitle,"(Creator)", section, subject, key, pic2))
                                    Log.d(ContentValues.TAG, "Class Name: $classTitle, Section: $section, Subject: $subject, Picture 2: $pic2")
                                }
                                if(classSnapshot.child("Students").hasChild(userId)){
                                    dataList.add(ClassDomain2(classTitle,"", section, subject, key, pic2))
                                }
                            }
                        }

                    classAdapter.updateData(dataList) // Update the adapter with the new data
                    dismissProgessBar()
                    if(classAdapter.itemCount > 0) {
                        binding.emptyLine.visibility = View.GONE
                        binding.emptyImage.visibility = View.GONE
                    }
                    else{
                        binding.emptyLine.visibility = View.VISIBLE
                        binding.emptyImage.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }

}