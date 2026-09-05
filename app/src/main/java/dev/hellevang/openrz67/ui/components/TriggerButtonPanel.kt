package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.hellevang.openrz67.R
import androidx.compose.ui.unit.dp
import dev.hellevang.openrz67.ui.theme.Dimens
import dev.hellevang.openrz67.viewmodel.TriggerControlViewModel

@Composable
fun TriggerButtonPanel(
    viewModel: TriggerControlViewModel,
    isConnected: Boolean
) {
    val triggerType by viewModel.triggerType.collectAsState()
    val startDelayedTrigger by viewModel.startDelayedTrigger.collectAsState()
    val countdownTimeLeft by viewModel.countdownTimeLeft.collectAsState()
    val countdownDuration by viewModel.countdownDuration.collectAsState()
    val isBulbActive by viewModel.isBulbActive.collectAsState()

    Row(
        modifier = Modifier.padding(top = Dimens.ModeButtonTopPadding)
    ) {
        if (isConnected) {
            ToggleButton(
                toggleButton = {
                    viewModel.toggleTriggerType()
                },
                text = stringResource(R.string.mode)
            )
        } else {
            Button(
                onClick = {
                    viewModel.reconnectBluetooth()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Dimens.ButtonEndPadding)
                )
                Text(stringResource(R.string.reconnect))
            }
        }
    }
    
    Text(
        fontSize = Dimens.SubHeaderTextSize,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(start = Dimens.StandardPadding, end = Dimens.StandardPadding),
        text = triggerType.name
    )
    
    Spacer(modifier = Modifier.padding(top = Dimens.SpacerTopPadding))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(start = Dimens.StandardPadding, end = Dimens.StandardPadding),
        contentAlignment = Alignment.Center
    ) {
        when (triggerType) {
            TriggerControlViewModel.TriggerType.Direct -> {
                Text(
                    fontSize = Dimens.BodyTextSize,
                    color = MaterialTheme.colorScheme.onBackground,
                    text = stringResource(R.string.direct_hint)
                )
            }
            TriggerControlViewModel.TriggerType.Countdown -> {
                CountdownDisplay(
                    startDelayedTrigger = startDelayedTrigger,
                    countdownTimeLeft = countdownTimeLeft,
                    countdownDuration = countdownDuration,
                    onDurationChange = { duration -> viewModel.setCountdownDuration(duration) }
                )
            }
            TriggerControlViewModel.TriggerType.Bulb -> {
                Text(
                    fontSize = Dimens.BodyTextSize,
                    color = MaterialTheme.colorScheme.onBackground,
                    text = stringResource(if (isBulbActive) R.string.bulb_hint_on else R.string.bulb_hint_off)
                )
            }
        }
    }

    Button(
        onClick = {
            viewModel.handleTriggerButtonClick()
        },
        modifier = Modifier
            .padding(top = Dimens.ButtonTopPadding),
        enabled = isConnected,
    ) {
        val buttonText = when (triggerType) {
            TriggerControlViewModel.TriggerType.Countdown -> if (startDelayedTrigger) R.string.stop else R.string.start
            TriggerControlViewModel.TriggerType.Bulb -> if (isBulbActive) R.string.turn_off else R.string.turn_on
            else -> R.string.trigger_shutter
        }
        Text(text = stringResource(buttonText), modifier = Modifier.padding(end = Dimens.ButtonEndPadding))
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun ToggleButton(
    toggleButton: () -> Unit,
    modifier: Modifier = Modifier,
    text: String
) {
    Button(onClick = { toggleButton() }, modifier = modifier) { Text(text) }
}