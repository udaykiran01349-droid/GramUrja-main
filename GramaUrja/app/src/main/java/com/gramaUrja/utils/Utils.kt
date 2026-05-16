package com.gramaUrja.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gramaUrja.R
import com.gramaUrja.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun getFreshnessText(lastUpdatedTimestamp: Long): String {
        if (lastUpdatedTimestamp == 0L) return "Not yet reported"
        val diffMs = System.currentTimeMillis() - lastUpdatedTimestamp
        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            seconds < 30 -> "Just now"
            seconds < 60 -> "${seconds}s ago"
            minutes < 60 -> "${minutes} min ago"
            hours < 24 -> "${hours}h ago"
            else -> "${hours / 24}d ago"
        }
    }

    fun getExactTimeText(timestamp: Long): String {
        if (timestamp == 0L) return "--:--"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

object PrefsUtils {
    private const val PREFS_NAME = "grama_urja_prefs"
    private const val KEY_ZONE_ID = "selected_zone_id"
    private const val KEY_ZONE_NAME = "selected_zone_name"

    fun saveZone(context: Context, zoneId: String, zoneName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ZONE_ID, zoneId)
            .putString(KEY_ZONE_NAME, zoneName)
            .apply()
    }

    fun getZoneId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ZONE_ID, null)

    fun getZoneName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ZONE_NAME, "Unknown Zone") ?: "Unknown Zone"
}

object NotificationUtils {
    const val CHANNEL_ID = "power_alerts_channel"
    const val CHANNEL_NAME = "Village Power Alerts"
    const val PUMP_CHANNEL_ID = "pump_timer_channel"
    const val PUMP_CHANNEL_NAME = "Pump Timer Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Power status channel
            val powerChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts for power status changes"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(powerChannel)

            // Pump timer channel
            val pumpChannel = NotificationChannel(PUMP_CHANNEL_ID, PUMP_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts when pump timer expires"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(pumpChannel)
            
            Log.d("NotificationUtils", "Notification channels created")
        }
    }

    fun showPowerOnNotification(context: Context, zoneName: String) {
        showNotification(context, "⚡ Power is ON!", "Power is back in $zoneName. Check your pumps!", R.drawable.ic_bolt)
    }

    fun showPowerOffNotification(context: Context, zoneName: String) {
        showNotification(context, "✗ Power is OFF", "Power cut reported in $zoneName.", R.drawable.ic_bolt_off)
    }

    fun showPumpTimerExpiredNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, PUMP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bolt) // Reusing icon for now
            .setContentTitle("Pump Timer Finished")
            .setContentText("The irrigation time is over. Please switch off your pump.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        notify(context, 2001, builder.build())
    }

    private fun showNotification(context: Context, title: String, message: String, icon: Int) {
        Log.d("NotificationUtils", "Attempting to show notification: $title")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        notify(context, System.currentTimeMillis().toInt(), builder.build())
    }

    private fun notify(context: Context, id: Int, notification: android.app.Notification) {
        try {
            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(id, notification)
                    Log.d("NotificationUtils", "Notification sent successfully")
                } else {
                    Log.w("NotificationUtils", "Notifications are DISABLED in system settings")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error showing notification: ${e.message}")
        }
    }
}
