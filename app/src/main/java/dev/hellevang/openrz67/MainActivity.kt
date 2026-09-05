package dev.hellevang.openrz67

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import dev.hellevang.openrz67.ui.components.HeaderComponent
import dev.hellevang.openrz67.ui.components.TriggerButtonPanel
import dev.hellevang.openrz67.ui.theme.Colors
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

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Colors.StatusBarColor.toColorInt()
        window.navigationBarColor = Colors.NavigationBarColor.toColorInt()

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

    Box {
        Image(
            painter = painterResource(R.drawable.d3fe691b34130991a5bf05a25d54d74300316eaff150963be736948feb5ec159),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }

    Column(
        modifier = Modifier.statusBarsPadding()
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
