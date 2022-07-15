package com.coaching.coaching

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.coaching.coaching.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var instance: MainActivity? = null
        const val PUSH_BASE_URL = "http://sqrt5.iptime.org:8081"
        const val WEB_SCHEMA_FULL_GET_FCMKEY =
            "sqrt://getFcmKey" // 자바 스크립트 콜 : javascript:fcmKey('\(PUSH_KEY)');
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        instance = this
        awaitPushToken()
        initWebView()
        val url = intent.getStringExtra("url")
        if (url.isNullOrBlank()) {
            binding.webview.loadUrl(PUSH_BASE_URL)
        } else {
            if(!url.isNullOrBlank()) {
                binding.webview.loadUrl(url)
            } else {
                binding.webview.loadUrl(PUSH_BASE_URL)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras?.getString("url")?.let { url ->
            if(url.isNotBlank()) {
                binding.webview.loadUrl(url)
            }
        }
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
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
        binding.webview.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return checkUrl(request?.url?.toString())
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return checkUrl(url)
            }
        }
    }

    private fun checkUrl(url: String?): Boolean {
        return when {
            url == WEB_SCHEMA_FULL_GET_FCMKEY -> {
                binding.webview.loadUrl("javascript:fcmKey(\"${AppPreferences.getPushToken(this@MainActivity)}\");")
                //push call
                //자바 스크립트 콜 : javascript:fcmKey('\(PUSH_KEY)');
                true
            }
            else -> false
        }
    }

    private fun awaitPushToken() {
        lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {
            val token  = Firebase.messaging.token.await()
            AppPreferences.setPushToken(this@MainActivity, token)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                binding.webview.onRequestPermissionResult()
            }
        }
    }
}