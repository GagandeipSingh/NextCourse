package com.example.nextcourse

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nextcourse.databinding.ActivityFilesViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FilesView : BaseActivity() {
    private lateinit var binding: ActivityFilesViewBinding
    private lateinit var dataList: ArrayList<Domain2>
    private lateinit var adapter2: Adapter2
    private lateinit var auth : FirebaseAuth
    private lateinit var bookBgDrawables : Array<Int>
    private lateinit var keyRef : String
    private lateinit var userId : String
    private lateinit var creator : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        keyRef = intent.getStringExtra("classRef").toString()
        auth = FirebaseAuth.getInstance()
        dataList = ArrayList()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bookBgDrawables = arrayOf(R.drawable.book_bg1, R.drawable.book_bg2, R.drawable.book_bg3, R.drawable.book_bg4, R.drawable.book_bg5,R.drawable.book_bg6)
        retrieveFiles()

        binding.shareSmall.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT,keyRef)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent,"Share: "))
        }

        binding.addFloat.setOnClickListener {
            val intent = Intent(this@FilesView,FileUploadActivity::class.java)
            Log.d("key:" ,keyRef)
            intent.putExtra("classRef",keyRef)
            startActivity(intent)
        }
        binding.addSmall.setOnClickListener {
            val intent = Intent(this@FilesView,FileUploadActivity::class.java)
            intent.putExtra("classRef",keyRef)
            intent.putExtra("creator",creator)
            startActivity(intent)
        }

//        Log.d("Key: ",intent.getStringExtra("classRef").toString())
//        dataList.add(Domain2("First","A: " + "12/10/24","L: " + "12/10/24",R.drawable.book_bg1))


        adapter2 = Adapter2(dataList,this)
        binding.FilesView.layoutManager = LinearLayoutManager(this)
        binding.FilesView.adapter = adapter2
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

        val databaseRef = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Files")
        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear() // Clear the existing list
                var count = 0
                    for (filesSnapshot in dataSnapshot.children) {
                        val filesData = filesSnapshot.getValue(FileDomain2::class.java)
                        filesData?.let {
                            val fileName = it.fileName
                            val adate = it.assignDate
                            val ldate = it.lastDate
                            val pic2 = bookBgDrawables[count % 6]
                            val fileUri = it.uri
                            count++
                            if(userId == creator){
                                dataList.add(Domain2(fileName, adate, ldate, pic2,fileUri))
                            }
                            else if(userId == filesSnapshot.key || creator == filesSnapshot.key){
                                dataList.add(Domain2(fileName, adate, ldate, pic2,fileUri))
                            }
                            else{

                            }
                        }
                    }
                    adapter2.notifyDataSetChanged() // Notify the adapter
                    dismissProgessBar()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "loadFiles:onCancelled", databaseError.toException())
            }
        })
    }
}