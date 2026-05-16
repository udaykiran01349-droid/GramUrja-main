package com.gramaUrja.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.gramaUrja.R
import com.gramaUrja.utils.NotificationUtils
import com.gramaUrja.utils.PrefsUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        NotificationUtils.createNotificationChannel(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val savedZoneId = PrefsUtils.getZoneId(this)
            if (savedZoneId != null) {
                // Go straight to main screen
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Zone not selected yet
                startActivity(Intent(this, ZoneSelectionActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
