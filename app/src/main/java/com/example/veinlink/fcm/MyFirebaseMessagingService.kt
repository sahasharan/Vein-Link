
package com.example.veinlink.fcm

import android.util.Log
import com.example.veinlink.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            
            // Since this is a simple notification, we'll use the existing helper.
            // For more complex notifications, you might create a different method.
            val notificationHelper = NotificationHelper(this)
            notificationHelper.createNotificationChannel() // Ensure channel is created
            notificationHelper.showRequestSentNotification() // We can customize this
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // TODO: Send this token to your server and store it against the user.
    }
}
