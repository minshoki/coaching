package com.coaching.coaching

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PushActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra("url")
        val i = if(MainActivity.instance == null) {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("url", url)
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("url", url)
            }
        }

        startActivity(i)
        finish()
    }
}