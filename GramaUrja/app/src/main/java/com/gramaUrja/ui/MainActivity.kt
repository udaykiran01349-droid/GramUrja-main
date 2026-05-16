package com.gramaUrja.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.gramaUrja.R
import com.gramaUrja.databinding.ActivityMainBinding
import com.gramaUrja.utils.NotificationUtils
import com.gramaUrja.utils.PrefsUtils
import com.gramaUrja.utils.TimeUtils
import com.gramaUrja.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var zoneId: String
    private lateinit var zoneName: String

    // Permission launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Toast.makeText(this, "Please enable notifications to receive power alerts", Toast.LENGTH_LONG).show()
        }
    }

    private val freshnessHandler = Handler(Looper.getMainLooper())
    private val freshnessRunnable = object : Runnable {
        override fun run() {
            updateFreshnessText()
            freshnessHandler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        zoneId = PrefsUtils.getZoneId(this) ?: run {
            startActivity(Intent(this, ZoneSelectionActivity::class.java))
            finish()
            return
        }
        zoneName = PrefsUtils.getZoneName(this)

        binding.tvZoneName.text = "📍 $zoneName"
        
        setupButtons()
        observeViewModel()
        checkNotificationPermission()
        subscribeToTopic()

        viewModel.startListening(zoneId)
        freshnessHandler.post(freshnessRunnable)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun subscribeToTopic() {
        // Users will receive notifications sent to this specific zone topic
        FirebaseMessaging.getInstance().subscribeToTopic("zone_$zoneId")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) "Subscribed to $zoneName alerts" else "Subscription failed"
                Log.d("FCM", msg)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        freshnessHandler.removeCallbacks(freshnessRunnable)
    }

    private fun setupButtons() {
        binding.btnPowerOn.setOnClickListener {
            viewModel.updatePowerStatus(zoneId, true, zoneName)
            // Test local notification immediately
            NotificationUtils.showPowerOnNotification(this, zoneName)
        }

        binding.btnPowerOff.setOnClickListener {
            viewModel.updatePowerStatus(zoneId, false, zoneName)
            // Test local notification for OFF status
            NotificationUtils.showPowerOffNotification(this, zoneName)
        }

        binding.btnChangeZone.setOnClickListener {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("zone_$zoneId")
            startActivity(Intent(this, ZoneSelectionActivity::class.java))
            finish()
        }

        binding.btnPumpTimer.setOnClickListener {
            startActivity(Intent(this, PumpTimerActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.powerStatus.collect { status ->
                status?.let {
                    updatePowerUI(it.isOn)
                    updateFreshnessText()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isUpdating.collect { isUpdating ->
                binding.progressUpdating.visibility = if (isUpdating) View.VISIBLE else View.GONE
                binding.btnPowerOn.isEnabled = !isUpdating
                binding.btnPowerOff.isEnabled = !isUpdating
            }
        }

        lifecycleScope.launch {
            viewModel.updateMessage.collect { msg ->
                msg?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearMessage()
                }
            }
        }
    }

    private fun updatePowerUI(isOn: Boolean) {
        if (isOn) {
            binding.tvPowerStatus.text = "POWER IS ON ⚡"
            binding.tvPowerStatus.setTextColor(getColor(R.color.power_on_green))
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.card_on_bg))
            binding.ivPowerIcon.setImageResource(R.drawable.ic_bolt)
            binding.ivPowerIcon.setColorFilter(getColor(R.color.power_on_green))
        } else {
            binding.tvPowerStatus.text = "POWER IS OFF ✗"
            binding.tvPowerStatus.setTextColor(getColor(R.color.power_off_red))
            binding.cardStatus.setCardBackgroundColor(getColor(R.color.card_off_bg))
            binding.ivPowerIcon.setImageResource(R.drawable.ic_bolt_off)
            binding.ivPowerIcon.setColorFilter(getColor(R.color.power_off_red))
        }
    }

    private fun updateFreshnessText() {
        val status = viewModel.powerStatus.value
        val ts = status?.lastUpdatedTimestamp ?: 0L
        binding.tvLastSeen.text = TimeUtils.getFreshnessText(ts)
        binding.tvExactTime.text = "Reported at: ${TimeUtils.getExactTimeText(ts)}"
    }
}
