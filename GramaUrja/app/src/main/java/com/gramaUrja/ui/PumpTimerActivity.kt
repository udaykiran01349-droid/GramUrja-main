package com.gramaUrja.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramaUrja.R
import com.gramaUrja.databinding.ActivityPumpTimerBinding
import com.gramaUrja.model.CropType
import com.gramaUrja.utils.NotificationUtils

class PumpTimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPumpTimerBinding
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var selectedMinutes = 30

    private val cropTypes = listOf(
        CropType("Rice / Paddy", 60, 120, "High water requirement"),
        CropType("Wheat", 30, 60, "Moderate water need"),
        CropType("Sugarcane", 45, 90, "High water requirement"),
        CropType("Vegetables", 20, 40, "Light watering needed"),
        CropType("Cotton", 30, 60, "Moderate water need"),
        CropType("Maize / Corn", 25, 50, "Moderate water need"),
        CropType("Groundnut", 20, 40, "Low to moderate need"),
        CropType("Soybean", 25, 45, "Moderate water need")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPumpTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCropSpinner()
        setupButtons()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCropSpinner() {
        val cropNames = cropTypes.map { it.name }
        val adapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_spinner_item, cropNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCrop.adapter = adapter

        binding.spinnerCrop.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val crop = cropTypes[position]
                selectedMinutes = (crop.minMinutes + crop.maxMinutes) / 2
                binding.tvCropInfo.text =
                    "${crop.description}\nRecommended: ${crop.minMinutes}–${crop.maxMinutes} min"
                binding.numberPickerMinutes.value = selectedMinutes
                updateTimerDisplay(selectedMinutes * 60 * 1000L)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        binding.numberPickerMinutes.minValue = 1
        binding.numberPickerMinutes.maxValue = 5
        binding.numberPickerMinutes.value = selectedMinutes
        binding.numberPickerMinutes.setOnValueChangedListener { _, _, newVal ->
            selectedMinutes = newVal
            if (!isTimerRunning) updateTimerDisplay(newVal * 60 * 1000L)
        }
    }

    private fun setupButtons() {
        binding.btnStartStop.setOnClickListener {
            if (isTimerRunning) stopTimer() else startTimer()
        }
        binding.btnReset.setOnClickListener { resetTimer() }
    }

    private fun startTimer() {
        val durationMs = selectedMinutes * 60 * 1000L
        isTimerRunning = true
        binding.btnStartStop.text = "⏹ Stop"
        binding.btnStartStop.setBackgroundColor(getColor(R.color.power_off_red))

        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.tvTimer.text = "00:00:00"
                binding.btnStartStop.text = "▶ Start"
                binding.btnStartStop.setBackgroundColor(getColor(R.color.power_on_green))
                
                // Show notification when timer expires
                NotificationUtils.showPumpTimerExpiredNotification(this@PumpTimerActivity)


            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        binding.btnStartStop.text = "▶ Start"
        binding.btnStartStop.setBackgroundColor(getColor(R.color.power_on_green))
    }

    private fun resetTimer() {
        stopTimer()
        selectedMinutes = binding.numberPickerMinutes.value
        updateTimerDisplay(selectedMinutes * 60 * 1000L)
    }

    private fun updateTimerDisplay(millisLeft: Long) {
        val totalSeconds = millisLeft / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
