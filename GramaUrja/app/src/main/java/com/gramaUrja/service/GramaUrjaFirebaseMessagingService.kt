package com.gramaUrja.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gramaUrja.utils.NotificationUtils
import com.gramaUrja.utils.PrefsUtils

class GramaUrjaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // 1. Handle Notification Payload (Sent from Firebase Console)
        remoteMessage.notification?.let {
            val title = it.title?.lowercase() ?: ""
            val body = it.body?.lowercase() ?: ""
            val zoneName = PrefsUtils.getZoneName(this)
            
            Log.d("FCM", "Notification Payload: Title=$title, Body=$body")

            when {
                title.contains("timer") || title.contains("pump") || body.contains("timer") -> {
                    NotificationUtils.showPumpTimerExpiredNotification(this)
                }
                title.contains("off") || body.contains("off") -> {
                    NotificationUtils.showPowerOffNotification(this, zoneName)
                }
                else -> {
                    NotificationUtils.showPowerOnNotification(this, zoneName)
                }
            }
            return 
        }

        // 2. Handle Data Payload (Sent via API/Cloud Functions)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Data Payload: ${remoteMessage.data}")
            val zoneName = remoteMessage.data["zoneName"] ?: PrefsUtils.getZoneName(this)
            val status = remoteMessage.data["status"]?.uppercase() ?: "ON"
            val type = remoteMessage.data["type"] ?: "power"

            when {
                type == "pump_timer" -> {
                    NotificationUtils.showPumpTimerExpiredNotification(this)
                }
                status == "OFF" -> {
                    NotificationUtils.showPowerOffNotification(this, zoneName)
                }
                else -> {
                    NotificationUtils.showPowerOnNotification(this, zoneName)
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }
}
