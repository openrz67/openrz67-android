package dev.hellevang.openrz67

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.hellevang.openrz67.ui.components.ControlPanel
import dev.hellevang.openrz67.ui.components.Header
import dev.hellevang.openrz67.ui.components.Stage
import dev.hellevang.openrz67.ui.theme.OpenRZ67Theme
import dev.hellevang.openrz67.viewmodel.TriggerControlViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TriggerControlViewModel by viewModels()

    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val enableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.startBluetooth()
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            ensureBluetoothEnabled()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        if (hasPermissions()) ensureBluetoothEnabled() else requestPermissions.launch(requiredPermissions)

        setContent {
            OpenRZ67Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OpenRZ67App(viewModel)
                }
            }
        }
    }

    private fun hasPermissions() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureBluetoothEnabled() {
        val adapter = getSystemService(BluetoothManager::class.java).adapter
        if (adapter?.isEnabled == true) {
            viewModel.startBluetooth()
        } else {
            enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}

@Composable
private fun OpenRZ67App(viewModel: TriggerControlViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val triggerType by viewModel.triggerType.collectAsState()
    val countdownRunning by viewModel.startDelayedTrigger.collectAsState()
    val countdownTimeLeft by viewModel.countdownTimeLeft.collectAsState()
    val countdownDuration by viewModel.countdownDuration.collectAsState()
    val isBulbActive by viewModel.isBulbActive.collectAsState()
    val bulbElapsed by viewModel.bulbElapsedSeconds.collectAsState()

    KeepScreenOn(countdownRunning || isBulbActive)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Header(connectionState, isConnected, onReconnect = viewModel::reconnectBluetooth)
        Stage(
            readout = when {
                countdownRunning -> countdownTimeLeft.toString()
                isBulbActive -> stringResource(R.string.elapsed_format, bulbElapsed / 60, bulbElapsed % 60)
                else -> null
            },
            modifier = Modifier.weight(1f)
        )
        ControlPanel(
            triggerType = triggerType,
            isConnected = isConnected,
            countdownRunning = countdownRunning,
            countdownDuration = countdownDuration,
            isBulbActive = isBulbActive,
            onSelectMode = viewModel::setTriggerType,
            onDurationChange = viewModel::setCountdownDuration,
            onShutter = viewModel::handleTriggerButtonClick
        )
    }
}

/** Keeps the display on while an exposure or countdown is in progress. */
@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val window = LocalActivity.current?.window ?: return
    DisposableEffect(enabled) {
        if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}
