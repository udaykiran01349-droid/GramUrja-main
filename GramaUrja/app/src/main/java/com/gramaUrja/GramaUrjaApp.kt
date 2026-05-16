package com.gramaUrja

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.gramaUrja.utils.NotificationUtils

class GramaUrjaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Firebase persistence early
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) {}

        // Create notification channel
        NotificationUtils.createNotificationChannel(this)
    }
}
