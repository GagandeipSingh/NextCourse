package com.example.nextcourse

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nextcourse.databinding.ActivityViewPdfBinding
import java.net.URLEncoder

class ViewPdf : BaseActivity() {
    private lateinit var binding : ActivityViewPdfBinding
    private lateinit var fileUri : String
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityViewPdfBinding.inflate(layoutInflater)
        fileUri = intent.getStringExtra("fileUri").toString()
        enableEdgeToEdge()
        setContentView(binding.root)
        showProgressBar()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.viewPdf.settings.javaScriptEnabled = true
        binding.viewPdf.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                dismissProgessBar()
            }
        }
        val url : String?
        try {
            url = URLEncoder.encode(fileUri,"UTF-8")
            binding.viewPdf.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
        }catch(e:Exception){
            Log.d("Exception: ",e.message.toString())
        }
    }
}