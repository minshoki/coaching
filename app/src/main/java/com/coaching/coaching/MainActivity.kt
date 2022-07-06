package com.coaching.coaching

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.coaching.coaching.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initWebView()
        binding.webview.loadUrl("http://sqrt5.iptime.org:8081")
    }

    private fun initWebView() {
        with(binding.webview.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true

            setSupportMultipleWindows(true)
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = true
            allowContentAccess = true
        }
    }
}