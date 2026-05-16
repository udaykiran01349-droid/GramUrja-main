package com.gramaUrja.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramaUrja.model.PowerStatus
import com.gramaUrja.model.Zone
import com.gramaUrja.repository.PowerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ZoneWithStatus(
    val zone: Zone,
    val status: PowerStatus?
)

class ZoneViewModel : ViewModel() {

    private val repository = PowerRepository()

    private val _zonesWithStatus = MutableStateFlow<List<ZoneWithStatus>>(emptyList())
    val zonesWithStatus: StateFlow<List<ZoneWithStatus>> = _zonesWithStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadZones()
    }

    private fun loadZones() {
        viewModelScope.launch {
            launch { repository.seedZones() }

            combine(
                repository.getZones(),
                repository.getAllPowerStatuses()
            ) { zones, statuses ->
                zones.map { zone ->
                    ZoneWithStatus(zone, statuses[zone.id])
                }
            }.collect { combinedList ->
                if (combinedList.isNotEmpty()) {
                    _zonesWithStatus.value = combinedList
                    _isLoading.value = false
                }
            }
        }
    }
}
