package com.example.nextcourse.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.domains.AnnounceDomain1
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.domains.FileDomain2
import com.example.nextcourse.R
import com.example.nextcourse.domains.UserDomain
import com.example.nextcourse.databinding.ActivityAnnouncementAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnnouncementAdd : BaseActivity() {
    private lateinit var binding : ActivityAnnouncementAddBinding
    private lateinit var storageReference: StorageReference
    private lateinit var announcementRefKey : DatabaseReference
    private lateinit var announcementReference: DatabaseReference
    private lateinit var filesReference: DatabaseReference
    private lateinit var keyRef:String
    private lateinit var userId:String
    private lateinit var auth: FirebaseAuth
    private lateinit var heading : String
    private lateinit var description : String
    private lateinit var uploader : String
    private lateinit var fileName : String
    private lateinit var date : String
    private var filepath: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityAnnouncementAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        keyRef = intent.getStringExtra("classRef").toString()
        userId = auth.currentUser?.uid.toString()
        storageReference = FirebaseStorage.getInstance().getReference()
        announcementReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements")
        announcementRefKey = announcementReference.push()
        filesReference = announcementRefKey.child("Files").child(userId)

        binding.attachCancel.setOnClickListener {
            filepath = null
            binding.attachCancel.visibility = View.GONE
            binding.etFileName.visibility = View.GONE
            binding.attachImage.setImageResource(R.drawable.attachment)
            binding.attachments.text = getString(R.string.add_attachment)
            binding.etFileName.text = null
        }
        binding.linearLayout2.setOnClickListener {
            launcher.launch("application/pdf")
        }
        binding.addBtn.setOnClickListener {
            heading = binding.etHeading.text.toString()
            description = binding.etDescription.text.toString()
            val database = Firebase.database
            val myRef = database.getReference("Users").child(userId)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userDomainData = dataSnapshot.getValue(UserDomain::class.java)
                    uploader = userDomainData?.name.toString()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("ClassesActivity", "Failed to get user name", databaseError.toException())
                }
            })
            fileName = binding.etFileName.text.toString()
            val calendar = Calendar.getInstance()
            val currentDate = calendar.time
            val simpleDateFormat = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
            date = simpleDateFormat.format(currentDate)
            if (validate(filepath,heading, description,fileName)) {
                process()
            }
        }
    }

    private fun process() {
        if(filepath == null){
            val announceVar = AnnounceDomain1(heading,description,date)
            announcementRefKey.setValue(announceVar)
            Toast.makeText(this@AnnouncementAdd,"Announcement Added..",Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            showProgressBar()
            val announceVar = AnnounceDomain1(heading,description,date)
            announcementRefKey.setValue(announceVar)
            val storageRef = storageReference.child("Files/" + System.currentTimeMillis() + ".pdf")
            storageRef.putFile(filepath!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener {
                        val fileKey = filesReference.push()
                        val fileVar = FileDomain2(fileName,"(Assigner)", uploader, date, it.toString(),fileKey.toString())
                        fileKey.setValue(fileVar)
                            .addOnSuccessListener {
                                dismissProgessBar()
                                Toast.makeText(this@AnnouncementAdd,"Announcement Added..",Toast.LENGTH_SHORT).show()
                                filepath = null
                                finish()
                            }
                    }
                }
        }

    }

    private fun validate(filepath:Uri?,heading:String, description:String,fileName:String): Boolean {
        var isValid = true
        if (heading == "") {
            binding.etHeading.error = "Set Heading.."
            isValid = false
        }
        if (description == "") {
            binding.etDescription.error = "Fill Description.."
            isValid = false
        }
        if(filepath != null){
            if (fileName == "") {
                binding.etFileName.error = "Enter File Name.."
                isValid = false
            }
        }
        return isValid
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        filepath = uri
        if(filepath!=null){
            binding.attachments.text = getString(R.string.file_selected)
            binding.attachCancel.visibility = View.VISIBLE
            binding.etFileName.visibility = View.VISIBLE
            binding.etFileName.requestFocus()
            binding.attachImage.setImageResource(R.drawable.filelogo)
        }
    }
}