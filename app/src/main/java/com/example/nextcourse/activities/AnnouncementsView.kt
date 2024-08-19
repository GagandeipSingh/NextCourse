package com.example.nextcourse.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nextcourse.adapters.AnnounceAdapter
import com.example.nextcourse.domains.AnnounceDomain1
import com.example.nextcourse.domains.AnnounceDomain2
import com.example.nextcourse.domains.ClassDomain1
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityAnnouncementsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AnnouncementsView : AppCompatActivity() {
    private lateinit var binding : ActivityAnnouncementsBinding
    private lateinit var dataList : ArrayList<AnnounceDomain2>
    private lateinit var keyRef : String
    private lateinit var classTitle : String
    private lateinit var creator: String
    private lateinit var userId : String
    private lateinit var section : String
    private lateinit var subject : String
    private lateinit var auth : FirebaseAuth
    private var picBg : Int? = 0
    private var isSameUser : Boolean = true
    private lateinit var announceAdapter : AnnounceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        keyRef = intent.getStringExtra("classRef").toString()
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid.toString()
        val classRef = FirebaseDatabase.getInstance().getReference("Classes").child(keyRef)
        classRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val classData =  dataSnapshot.getValue(ClassDomain1::class.java)
                classData?.let {
                    creator = it.creator
                }
                if(userId != creator){
                    binding.addFloat.visibility = View.GONE
                    isSameUser = false
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
        classTitle = intent.getStringExtra("class").toString()
        binding.Classtitle.text = classTitle
        section = intent.getStringExtra("sec").toString()
        binding.Section.text = section
        subject = intent.getStringExtra("sub").toString()
        binding.Subject.text = subject
        picBg = intent.getIntExtra("bg", R.drawable.book_bg4)
        binding.ImageBehind.setImageResource(picBg!!)
        dataList = ArrayList()
        retrieveAnnouncements()
        binding.addFloat.setOnClickListener {
            val intent = Intent(this@AnnouncementsView, AnnouncementAdd::class.java)
            intent.putExtra("classRef",keyRef)
            startActivity(intent)
        }
        binding.cancel.setOnClickListener {
            finish()
        }
    }
    private fun retrieveAnnouncements() {
        if (userId != "") {
            val database = Firebase.database
            val announcementsRef = database.getReference("Classes").child("$keyRef/Announcements")
            announcementsRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear() // Clear the existing list
                    for (announcementSnapshot in dataSnapshot.children) {
                            val announceData = announcementSnapshot.getValue(AnnounceDomain1::class.java)
                            announceData?.let {
                                val heading = it.heading
                                val description = it.description
                                val adate = it.adate
                                val announceKey = announcementSnapshot.key.toString()
                                dataList.add(AnnounceDomain2(heading,description,adate,keyRef,announceKey))
                        }
                    }
                    binding.announcementView.layoutManager = LinearLayoutManager(this@AnnouncementsView)
                    announceAdapter = AnnounceAdapter(this@AnnouncementsView,dataList,isSameUser)
                    binding.announcementView.adapter = announceAdapter
                    announceAdapter.notifyDataSetChanged()
                    if(announceAdapter.itemCount > 0) {
                        binding.emptyLine.visibility = View.GONE
                        binding.emptyImage.visibility = View.GONE
                    }
                    else{
                        binding.emptyLine.visibility = View.VISIBLE
                        binding.emptyImage.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(ContentValues.TAG, "loadFiles:onCancelled", databaseError.toException())
                }
            })
        }
    }
}