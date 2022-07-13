package com.coaching.coaching

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannelCompat.Builder(
            "my_default_channel_id",
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName("알림")
            .setShowBadge(true)
            .setVibrationPattern(longArrayOf(0))
            .setVibrationEnabled(true)
            .build()

        NotificationManagerCompat.from(this).createNotificationChannel(channel)

    }

}