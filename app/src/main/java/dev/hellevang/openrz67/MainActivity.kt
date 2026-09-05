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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import dev.hellevang.openrz67.ui.components.HeaderComponent
import dev.hellevang.openrz67.ui.components.TriggerButtonPanel
import dev.hellevang.openrz67.ui.theme.Dimens
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
            Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Background is always light, so force dark system bar icons
        val bars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        if (hasPermissions()) ensureBluetoothEnabled() else requestPermissions.launch(requiredPermissions)

        setContent {
            OpenRZ67Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
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
    val countdownRunning by viewModel.startDelayedTrigger.collectAsState()
    val isBulbActive by viewModel.isBulbActive.collectAsState()

    KeepScreenOn(countdownRunning || isBulbActive)

    Box {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }

    Column(
        modifier = Modifier.systemBarsPadding()
    ) {
        HeaderComponent()
        Spacer(modifier = Modifier.padding(top = Dimens.TopSectionPadding))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.TopSectionPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(connectionState, color = MaterialTheme.colors.onBackground)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = Dimens.TopSectionPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TriggerButtonPanel(viewModel, isConnected)
        }
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

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    OpenRZ67Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            HeaderComponent()
        }
    }
}
