package com.coaching.coaching

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        notify(message)
    }

    private fun notify(message: RemoteMessage) {
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(this)

        val title = message.data["title"] ?: ""
        val body = message.data["body"] ?: ""
        val url = message.data["url"]

        val mainIntent = Intent(this, PushActivity::class.java).apply {
            if(url.isNullOrBlank()) putExtra("url", url)
            else {
                putExtra("url", MainActivity.PUSH_BASE_URL + url)
            }
        }

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification: Notification = NotificationCompat.Builder(this, "my_default_channel_id")
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true).apply {

                }.build()


        manager.notify(1, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppPreferences.setPushToken(this, token)
    }
}