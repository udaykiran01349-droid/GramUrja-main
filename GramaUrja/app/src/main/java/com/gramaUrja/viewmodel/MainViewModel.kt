package com.gramaUrja.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gramaUrja.model.PowerStatus
import com.gramaUrja.model.Zone
import com.gramaUrja.repository.PowerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = PowerRepository()

    private val _powerStatus = MutableStateFlow<PowerStatus?>(null)
    val powerStatus: StateFlow<PowerStatus?> = _powerStatus.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    fun startListening(zoneId: String) {
        viewModelScope.launch {
            repository.getPowerStatus(zoneId).collect { status ->
                _powerStatus.value = status
            }
        }
    }

    fun updatePowerStatus(zoneId: String, isOn: Boolean, zoneName: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            val success = repository.updatePowerStatus(zoneId, isOn, zoneName)
            _isUpdating.value = false
            _updateMessage.value = if (success) {
                if (isOn) "Power ON reported! Alert sent to your zone." else "Power OFF reported. Zone notified."
            } else {
                "Update failed. Check your connection."
            }
        }
    }

    fun clearMessage() {
        _updateMessage.value = null
    }
}
