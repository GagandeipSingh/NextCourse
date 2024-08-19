package com.example.nextcourse.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nextcourse.adapters.FileAdapter
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.domains.ClassDomain1
import com.example.nextcourse.domains.FileDomain1
import com.example.nextcourse.domains.FileDomain2
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityFilesViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FilesView : BaseActivity() {
    private lateinit var binding: ActivityFilesViewBinding
    private lateinit var dataList: ArrayList<FileDomain1>
    private lateinit var fileAdapter: FileAdapter
    private lateinit var auth : FirebaseAuth
    private lateinit var keyRef : String
    private lateinit var userId : String
    private lateinit var creator : String
    private var isSameUser : Boolean = false
    private lateinit var aKey : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        keyRef = intent.getStringExtra("keyRef").toString()
        aKey = intent.getStringExtra("AnnounceKey").toString()
        auth = FirebaseAuth.getInstance()
        dataList = ArrayList()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        retrieveFiles()

        binding.addFloat.setOnClickListener {
            val intent = Intent(this@FilesView, FileUploadActivity::class.java)
            intent.putExtra("classRef",keyRef)
            intent.putExtra("aKey",aKey)
            intent.putExtra("SameUser",isSameUser)
            startActivity(intent)
        }
        binding.addSmall.setOnClickListener {
            val intent = Intent(this@FilesView, FileUploadActivity::class.java)
            intent.putExtra("classRef",keyRef)
            intent.putExtra("aKey",aKey)
            intent.putExtra("SameUser",isSameUser)
            startActivity(intent)
        }

    }

    private fun retrieveFiles() {
        showProgressBar()
        userId = auth.currentUser?.uid.toString()
        val classRef = FirebaseDatabase.getInstance().getReference("Classes").child(keyRef)
        classRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val classData =  dataSnapshot.getValue(ClassDomain1::class.java)
                    classData?.let {
                        creator = it.creator
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        val databaseRef = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements/$aKey/Files")
        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear() // Clear the existing list
                    for (filesUSnapshot in dataSnapshot.children) {
                        for(filesSnapshot in filesUSnapshot.children){
                            val filesData = filesSnapshot.getValue(FileDomain2::class.java)
                            filesData?.let {
                                val fileName = it.fileName
                                val assigner = it.assigner
                                val uploader = it.uploader
                                val date = it.date
                                val fileUri = it.uri
                                val fileKey = it.fileKey
                                if(userId == creator){
                                    dataList.add(FileDomain1(fileName,assigner, uploader,date,fileUri,fileKey))
                                }
                                else if(userId == filesUSnapshot.key || creator == filesUSnapshot.key){
                                    dataList.add(FileDomain1(fileName,assigner, uploader, date,fileUri,fileKey))
                                }
                                else{

                                }
                            }
                        }
                    }
                if(userId == creator){ isSameUser = true }
                    fileAdapter = FileAdapter(dataList,this@FilesView,isSameUser)
                    binding.FilesView.layoutManager = LinearLayoutManager(this@FilesView)
                    binding.FilesView.adapter = fileAdapter
                    fileAdapter.notifyDataSetChanged() // Notify the adapter
                    dismissProgessBar()
                if(fileAdapter.itemCount > 0) {
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