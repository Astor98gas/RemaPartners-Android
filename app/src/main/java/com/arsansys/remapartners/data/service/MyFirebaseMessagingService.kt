package com.arsansys.remapartners.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arsansys.remapartners.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var channelId: String

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Crear la notificación
        createNotificationChannel()
        showNotification(message.notification?.title, message.notification?.body)
    }

    private fun createNotificationChannel() {
        val name = "Rema Partners Channel"
        val descriptionText = "Channel for rema partners notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        channelId = "rema_partners_channel_id" + Random.nextInt(0, 1000)
        val channel = NotificationChannel(
            channelId,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationId = Random.nextInt(1000) // Genera un número aleatorio entre 0 y 999
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (NotificationManagerCompat.from(this@MyFirebaseMessagingService)
                    .areNotificationsEnabled()
            ) {
                try {
                    notify(notificationId, builder.build())
                } catch (e: SecurityException) {
                    Log.e(TAG, "Notification permission not granted", e)
                }
            } else {
                Log.w(TAG, "Notifications are disabled")
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
}