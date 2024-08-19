package com.example.nextcourse.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.R
import com.example.nextcourse.databinding.ActivityAnnounceDescViewBinding

class AnnounceDescView : AppCompatActivity() {
    private lateinit var binding : ActivityAnnounceDescViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnnounceDescViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val heading = intent.getStringExtra("Heading")
        val description = intent.getStringExtra("Description")
        val date = intent.getStringExtra("Date")
        val keyRef = intent.getStringExtra("keyRef")
        val aKey = intent.getStringExtra("AnnounceKey")
        binding.Heading.text = heading
        binding.description.text = description
        binding.adate.text = getString(R.string.added_on,date)
        binding.linearLayout.setOnClickListener {
            val intent = Intent(this@AnnounceDescView, FilesView::class.java)
            intent.putExtra("keyRef",keyRef)
            intent.putExtra("AnnounceKey",aKey)
            startActivity(intent)
        }
        binding.cancel.setOnClickListener {
            finish()
        }
    }
}