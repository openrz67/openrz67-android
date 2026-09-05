package dev.hellevang.openrz67.bluetooth

import android.util.Log
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

/**
 * Owns the BLE connection to the openrz67-trigger. The peripheral is bound to [scope];
 * cancelling the scope disconnects it and stops the reconnect loop.
 */
class BluetoothManager(private val scope: CoroutineScope) {
    private lateinit var peripheral: Peripheral
    private val connectionAttempt = AtomicInteger()

    private val _connectionState = MutableStateFlow("Not connected")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    companion object {
        private const val TAG = "BluetoothManager"
        val TARGET_SERVICE_UUID: UUID = UUID.fromString("c9239c9e-6fc9-4168-b3aa-53105eb990b0")
        val TARGET_CHARACTERISTIC_UUID: UUID = UUID.fromString("458d4dc9-349f-401d-b092-a2b1c55f5319")
    }

    private val characteristic = characteristicOf(
        TARGET_SERVICE_UUID.toString(),
        TARGET_CHARACTERISTIC_UUID.toString()
    )

    /** Firmware command codes: base value = release/off, base + 1 = press/on. */
    enum class SignalType(val base: Int) {
        Trigger(10),
        BulbMode(20)
    }

    fun initialize() {
        scope.launch {
            try {
                _connectionState.value = "Scanning for devices..."
                val advertisement = Scanner {
                    filters = listOf(Filter.Service(TARGET_SERVICE_UUID))
                }.advertisements.first()

                _connectionState.value = "Device found, connecting..."
                peripheral = scope.peripheral(advertisement)

                enableAutoReconnect()
                connect()

                peripheral.state.collect { state ->
                    _connectionState.value = state.toString()
                    _isConnected.value = state is State.Connected
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize Bluetooth", e)
                _connectionState.value = "Failed to initialize: ${e.message}"
            }
        }
    }

    private fun CoroutineScope.enableAutoReconnect() {
        peripheral.state
            .filter { it is State.Disconnected }
            .onEach {
                val attempt = connectionAttempt.getAndIncrement()
                val waitMillis = minOf((500L * 2f.pow(attempt)).toLong(), 30_000L)
                Log.d(TAG, "Reconnecting in $waitMillis ms (attempt ${attempt + 1})")
                delay(waitMillis)
                connect()
            }
            .launchIn(this)
    }

    private fun CoroutineScope.connect() {
        launch {
            try {
                peripheral.connect()
                connectionAttempt.set(0)
            } catch (e: Exception) {
                Log.w(TAG, "Connection attempt failed: ${e.message}")
            }
        }
    }

    /** Throws if the write fails; callers decide how to surface that. */
    suspend fun sendCountdown(durationSeconds: Int, start: Boolean) {
        val action: Byte = if (start) 1 else 0
        peripheral.write(characteristic, byteArrayOf(3, durationSeconds.toByte(), action))
    }

    suspend fun sendSignal(signalType: SignalType, on: Boolean = true) {
        val code = signalType.base + if (on) 1 else 0
        peripheral.write(characteristic, byteArrayOf(code.toByte()))
    }

    fun manualReconnect() {
        connectionAttempt.set(0)
        if (::peripheral.isInitialized) {
            _connectionState.value = "Manual reconnection..."
            scope.connect()
        } else {
            initialize()
        }
    }
}
