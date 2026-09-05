package dev.hellevang.openrz67.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.hellevang.openrz67.bluetooth.BluetoothLink
import dev.hellevang.openrz67.bluetooth.BluetoothManager
import dev.hellevang.openrz67.bluetooth.SignalType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriggerControlViewModel(
    link: (CoroutineScope) -> BluetoothLink = ::BluetoothManager
) : ViewModel() {

    private val bluetoothManager = link(viewModelScope)
    private var bluetoothStarted = false

    private val _triggerType = MutableStateFlow(TriggerType.Direct)
    val triggerType: StateFlow<TriggerType> = _triggerType.asStateFlow()

    private val _startDelayedTrigger = MutableStateFlow(false)
    val startDelayedTrigger: StateFlow<Boolean> = _startDelayedTrigger.asStateFlow()

    private val _countdownTimeLeft = MutableStateFlow(0)
    val countdownTimeLeft: StateFlow<Int> = _countdownTimeLeft.asStateFlow()

    private val _countdownDuration = MutableStateFlow(10)
    val countdownDuration: StateFlow<Int> = _countdownDuration.asStateFlow()

    private val _isBulbActive = MutableStateFlow(false)
    val isBulbActive: StateFlow<Boolean> = _isBulbActive.asStateFlow()

    private val _bulbElapsedSeconds = MutableStateFlow(0)
    val bulbElapsedSeconds: StateFlow<Int> = _bulbElapsedSeconds.asStateFlow()

    val connectionState: StateFlow<String> = bluetoothManager.connectionState
    val isConnected: StateFlow<Boolean> = bluetoothManager.isConnected

    private var countdownJob: Job? = null
    private var bulbJob: Job? = null

    enum class TriggerType {
        Direct,
        Countdown,
        Bulb
    }

    /** Call once permissions are granted and Bluetooth is on. Safe to call repeatedly. */
    fun startBluetooth() {
        if (bluetoothStarted) return
        bluetoothStarted = true
        bluetoothManager.initialize()
    }

    fun reconnectBluetooth() {
        bluetoothStarted = true
        bluetoothManager.manualReconnect()
    }

    fun setTriggerType(type: TriggerType) {
        if (type == _triggerType.value) return
        if (_isBulbActive.value) setBulb(false)
        if (_startDelayedTrigger.value) stopCountdown()
        _triggerType.value = type
    }

    fun handleTriggerButtonClick() {
        when (_triggerType.value) {
            TriggerType.Direct -> send { bluetoothManager.sendSignal(SignalType.Trigger) }
            TriggerType.Countdown -> if (_startDelayedTrigger.value) stopCountdown() else startCountdown()
            TriggerType.Bulb -> setBulb(!_isBulbActive.value)
        }
    }

    fun setCountdownDuration(duration: Int) {
        if (duration in 1..255) {
            _countdownDuration.value = duration
        }
    }

    private fun setBulb(on: Boolean) = send(onSuccess = {
        _isBulbActive.value = on
        bulbJob?.cancel()
        _bulbElapsedSeconds.value = 0
        if (on) {
            bulbJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _bulbElapsedSeconds.value += 1
                }
            }
        }
    }) {
        bluetoothManager.sendSignal(SignalType.BulbMode, on)
    }

    private fun startCountdown() = send(onSuccess = { startCountdownTimer() }) {
        bluetoothManager.sendCountdown(_countdownDuration.value, true)
    }

    private fun stopCountdown() {
        stopCountdownTimer()
        send { bluetoothManager.sendCountdown(_countdownDuration.value, false) }
    }

    /** Runs a BLE write; only updates local state via [onSuccess] if the write succeeded. */
    private fun send(onSuccess: () -> Unit = {}, write: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                write()
                onSuccess()
            } catch (e: Exception) {
                Log.w("TriggerControlViewModel", "BLE write failed: ${e.message}")
            }
        }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        val duration = _countdownDuration.value
        _startDelayedTrigger.value = true
        _countdownTimeLeft.value = duration
        countdownJob = viewModelScope.launch {
            repeat(duration) {
                delay(1000)
                _countdownTimeLeft.value = _countdownTimeLeft.value - 1
            }
            _startDelayedTrigger.value = false
        }
    }

    private fun stopCountdownTimer() {
        countdownJob?.cancel()
        _startDelayedTrigger.value = false
        _countdownTimeLeft.value = 0
    }
}
