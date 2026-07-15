package dev.hellevang.openrz67.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hellevang.openrz67.bluetooth.BluetoothManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriggerControlViewModel(
    private val bluetoothManager: BluetoothManager
) : ViewModel() {
    
    // Trigger type state
    private val _triggerType = MutableStateFlow(TriggerType.Direct)
    val triggerType: StateFlow<TriggerType> = _triggerType.asStateFlow()
    
    // Countdown state
    private val _startDelayedTrigger = MutableStateFlow(false)
    val startDelayedTrigger: StateFlow<Boolean> = _startDelayedTrigger.asStateFlow()
    
    private val _countdownTimeLeft = MutableStateFlow(0)
    val countdownTimeLeft: StateFlow<Int> = _countdownTimeLeft.asStateFlow()
    
    private val _countdownDuration = MutableStateFlow(10)
    val countdownDuration: StateFlow<Int> = _countdownDuration.asStateFlow()
    
    // Bulb mode state
    private val _isBulbActive = MutableStateFlow(false)
    val isBulbActive: StateFlow<Boolean> = _isBulbActive.asStateFlow()
    
    // Bluetooth state (forwarded from BluetoothManager)
    val connectionState: StateFlow<String> = bluetoothManager.connectionState
    val isConnected: StateFlow<Boolean> = bluetoothManager.isConnected
    
    private var countdownJob: Job? = null
    
    enum class TriggerType {
        Direct,
        Countdown,
        Bulb
    }
    
    fun toggleTriggerType() {
        // If leaving bulb mode and it's active, turn it off
        if (_triggerType.value == TriggerType.Bulb && _isBulbActive.value) {
            _isBulbActive.value = false
            bluetoothManager.sendSignal(BluetoothManager.SignalType.BulbMode, false)
        }

        // If leaving countdown mode while a countdown is running, cancel it —
        // otherwise the trigger keeps blinking and fires when it expires
        if (_triggerType.value == TriggerType.Countdown && _startDelayedTrigger.value) {
            stopCountdown()
        }

        _triggerType.value = when (_triggerType.value) {
            TriggerType.Direct -> TriggerType.Countdown
            TriggerType.Countdown -> TriggerType.Bulb
            TriggerType.Bulb -> TriggerType.Direct
        }
    }
    
    fun handleTriggerButtonClick() {
        when (_triggerType.value) {
            TriggerType.Direct -> {
                bluetoothManager.sendSignal(BluetoothManager.SignalType.Trigger)
            }
            TriggerType.Countdown -> {
                if (_startDelayedTrigger.value) {
                    // Cancel countdown
                    stopCountdown()
                } else {
                    // Start countdown
                    startCountdown()
                }
            }
            TriggerType.Bulb -> {
                toggleBulbMode()
            }
        }
    }
    
    fun setCountdownDuration(duration: Int) {
        if (duration in 1..255) {
            _countdownDuration.value = duration
        }
    }
    
    private fun toggleBulbMode() {
        val newState = !_isBulbActive.value
        _isBulbActive.value = newState
        bluetoothManager.sendSignal(BluetoothManager.SignalType.BulbMode, newState)
    }
    
    fun reconnectBluetooth() {
        bluetoothManager.manualReconnect()
    }
    
    private fun startCountdown() {
        bluetoothManager.sendMultiByteCountdown(_countdownDuration.value, true)
        _startDelayedTrigger.value = true
        startCountdownTimer()
    }
    
    private fun stopCountdown() {
        bluetoothManager.sendMultiByteCountdown(_countdownDuration.value, false)
        _startDelayedTrigger.value = false
        stopCountdownTimer()
    }
    
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        val duration = _countdownDuration.value
        _countdownTimeLeft.value = duration
        countdownJob = viewModelScope.launch {
            repeat(duration) {
                delay(1000)
                _countdownTimeLeft.value = _countdownTimeLeft.value - 1
            }
            // Countdown finished
            _startDelayedTrigger.value = false
            _countdownTimeLeft.value = 0
        }
    }
    
    private fun stopCountdownTimer() {
        countdownJob?.cancel()
        _countdownTimeLeft.value = 0
    }
    
    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        bluetoothManager.cleanup()
    }
}