package com.example.nextcourse.activities

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.nextcourse.progress.BaseActivity
import com.example.nextcourse.domains.FileDomain2
import com.example.nextcourse.R
import com.example.nextcourse.domains.UserDomain
import com.example.nextcourse.databinding.ActivityFileUploadBinding
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

class FileUploadActivity : BaseActivity() {
    private lateinit var binding: ActivityFileUploadBinding
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var filepath: Uri? = null
    private lateinit var fileName: String
    private lateinit var assigner: String
    private var uploader: String = ""
    private var date: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private lateinit var keyRef:String
    private lateinit var aKey:String
    private var sameUser : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)
        val scrollView = binding.scrollView
        keyRef = intent.getStringExtra("classRef").toString()
        aKey = intent.getStringExtra("aKey").toString()
        userId = auth.currentUser?.uid.toString()
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
        sameUser = intent.getBooleanExtra("SameUser",false)
        if(!sameUser){
            binding.assigner.text = getString(R.string.nullStr)
        }
        val uploaderFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, v.bottom + binding.scrollView.paddingBottom)
                    binding.uploader.setText(uploader)
                    binding.uploader.clearFocus()
                    binding.tilUploader.isErrorEnabled = false
                }
            }
        }

        val dateFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, v.bottom + binding.scrollView.paddingBottom)
                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.time
                    val simpleDateFormat = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
                    date = simpleDateFormat.format(currentDate)
                    binding.date.setText(date)
                    binding.date.clearFocus()
                    binding.tilDate.isErrorEnabled = false
                }
            }
        }

        binding.uploader.onFocusChangeListener = uploaderFocusListener
        binding.date.onFocusChangeListener = dateFocusListener

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.uploader.doOnTextChanged { _, _, _, _ ->
            binding.tilUploader.isErrorEnabled = false
        }
        binding.fileName.doOnTextChanged { _, _, _, _ ->
            binding.tilFileName.isErrorEnabled = false
        }
        storageReference = FirebaseStorage.getInstance().getReference()
        databaseReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Announcements/$aKey/Files").child(userId)
        binding.uploadSelected.visibility = View.INVISIBLE
        binding.cancel.visibility = View.INVISIBLE
        binding.uploadData.setOnClickListener {
            launcher.launch("application/pdf")
        }

        binding.saveButton.setOnClickListener {
            fileName = binding.fileName.text.toString()
            assigner = binding.assigner.text.toString()
            if (validateFile(fileName,uploader,date)) {
                processFile(fileName,assigner, uploader, date)
            }
        }

        binding.cancel.setOnClickListener {
            filepath = null
            binding.uploadSelected.visibility = View.INVISIBLE
            binding.cancel.visibility = View.INVISIBLE
            binding.uploadData.visibility = View.VISIBLE
            binding.FileHeading.text = getString(R.string.uploadContent)
        }
    }

    private fun processFile(fileName: String, assigner:String, uploader: String, date: String) {
        showProgressBar()
        val storageRef = storageReference.child("Files/" + System.currentTimeMillis() + ".pdf")
        storageRef.putFile(filepath!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener {
                    val fileKey = databaseReference.push()
                    val fileVar = FileDomain2(fileName,assigner, uploader, date, it.toString(),fileKey.toString())
                    fileKey.setValue(fileVar)
                        .addOnSuccessListener {
                            Toast.makeText(this@FileUploadActivity,"File Uploaded..",Toast.LENGTH_SHORT).show()
                            filepath = null
                            finish()
                            dismissProgessBar()
                        }
                }
            }
            .addOnProgressListener {
                //
            }
    }

    private fun validateFile(fileName: String, uploader : String,date : String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(fileName)) {
            binding.tilFileName.error = "Enter File Name"
            isValid = false
        }

        if (TextUtils.isEmpty(uploader)) {
            binding.tilUploader.error = "Enter Name"
            isValid = false
        }

        if (TextUtils.isEmpty(date)) {
            binding.tilDate.error = "Enter Date"
            isValid = false
        }

        if (filepath == null) {
            Toast.makeText(this@FileUploadActivity,"Select the File..",Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
        filepath = uri
        if(filepath!=null){
            binding.FileHeading.text = getString(R.string.file_selected)
            binding.uploadSelected.visibility = View.VISIBLE
            binding.cancel.visibility = View.VISIBLE
            binding.uploadData.visibility = View.INVISIBLE
        }
    }
}
