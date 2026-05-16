package com.gramaUrja.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gramaUrja.databinding.ActivityZoneSelectionBinding
import com.gramaUrja.model.Zone
import com.gramaUrja.utils.PrefsUtils
import com.gramaUrja.viewmodel.ZoneViewModel
import kotlinx.coroutines.launch

class ZoneSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityZoneSelectionBinding
    private val viewModel: ZoneViewModel by viewModels()
    private lateinit var adapter: ZoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZoneSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeZones()
    }

    private fun setupRecyclerView() {
        adapter = ZoneAdapter { zone -> onZoneSelected(zone) }
        binding.rvZones.layoutManager = LinearLayoutManager(this)
        binding.rvZones.adapter = adapter
    }

    private fun observeZones() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.rvZones.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
        lifecycleScope.launch {
            viewModel.zonesWithStatus.collect { zonesWithStatus ->
                adapter.submitList(zonesWithStatus)
            }
        }
    }

    private fun onZoneSelected(zone: Zone) {
        PrefsUtils.saveZone(this, zone.id, zone.name)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
