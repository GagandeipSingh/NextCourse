package com.example.nextcourse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.databinding.ActivityFileUploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class FileUploadActivity : BaseActivity() {
    private lateinit var binding: ActivityFileUploadBinding
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var filepath: Uri? = null
    private lateinit var fileName: String
    private lateinit var adate: String
    private lateinit var ldate: String
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private lateinit var keyRef:String
    private lateinit var creator:String
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)
        val scrollView = binding.scrollView
        keyRef = intent.getStringExtra("classRef").toString()
        creator = intent.getStringExtra("creator").toString()
        userId = auth.currentUser?.uid.toString()
        if(userId != creator){
            binding.adate.setText(getString(R.string.empty))
            binding.adate.visibility = View.GONE
            val density = resources.displayMetrics.density
            val pixelValue = 20 * density
            val layoutParams = binding.tilLDate.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = pixelValue.toInt()
            binding.tilLDate.layoutParams = layoutParams
        }
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

        binding.adate.onFocusChangeListener = sectionFocusListener
        binding.ldate.onFocusChangeListener = subjectFocusListener

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        storageReference = FirebaseStorage.getInstance().getReference()
        databaseReference = FirebaseDatabase.getInstance().getReference("Classes").child("$keyRef/Files")
        binding.uploadSelected.visibility = View.INVISIBLE
        binding.cancel.visibility = View.INVISIBLE

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    filepath = result.data!!.data
                    binding.FileHeading.text = getString(R.string.file_selected)
                    binding.uploadSelected.visibility = View.VISIBLE
                    binding.cancel.visibility = View.VISIBLE
                    binding.uploadData.visibility = View.INVISIBLE
                }
            }
        }

        binding.uploadData.setOnClickListener {
            Dexter.withContext(this@FileUploadActivity)
                .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                        }
                        activityResultLauncher.launch(Intent.createChooser(intent, "Select Pdf File.."))
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        // Permission is denied. You can inform the user here
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {
                        // This method is called when the user denies the permission for the first time,
                        // but hasn't marked the "Don't ask again" checkbox.
                        Toast.makeText(this@FileUploadActivity,"Permission is required..",Toast.LENGTH_SHORT).show()
                        token.continuePermissionRequest()
                    }
                }).check()
        }

        binding.saveButton.setOnClickListener {
            fileName = binding.fileName.text.toString()
            adate = binding.adate.text.toString()
            ldate = binding.ldate.text.toString()
            if (validateFile(fileName, adate, ldate)) {
                processFile(fileName, adate, ldate)
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

    private fun processFile(fileName: String, adate: String, ldate: String) {
        showProgressBar()
        val storageRef = storageReference.child("Files/" + System.currentTimeMillis() + ".pdf")
        storageRef.putFile(filepath!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener {
                    val fileVar = FileDomain2(fileName, adate, ldate, it.toString())
//                    val key = databaseReference.push().key
                    val key = userId
                    databaseReference.child(key).setValue(fileVar)
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

    private fun validateFile(fileName: String, adate: String, ldate: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(fileName)) {
            binding.tilFileName.error = "Enter File Name"
            isValid = false
        } else {
            binding.tilFileName.error = null
        }

        if (TextUtils.isEmpty(adate) && userId == creator) {
            binding.tilAssignDate.error = "Enter Assignment Date"
            isValid = false
        } else {
            binding.tilAssignDate.error = null
        }

        if (TextUtils.isEmpty(ldate)) {
            binding.tilLDate.error = "Enter Submission Date"
            isValid = false
        } else {
            binding.tilLDate.error = null
        }

        if (filepath == null) {
            Toast.makeText(this@FileUploadActivity,"Select the File..",Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }
}
