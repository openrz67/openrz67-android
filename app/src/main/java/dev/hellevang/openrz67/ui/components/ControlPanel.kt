package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.hellevang.openrz67.R
import dev.hellevang.openrz67.viewmodel.TriggerControlViewModel.TriggerType

private val durationOptions = listOf(2, 5, 10, 15, 30, 60, 120, 180, 240)

@Composable
fun ControlPanel(
    triggerType: TriggerType,
    isConnected: Boolean,
    countdownRunning: Boolean,
    countdownDuration: Int,
    isBulbActive: Boolean,
    onSelectMode: (TriggerType) -> Unit,
    onDurationChange: (Int) -> Unit,
    onShutter: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TriggerType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = type == triggerType,
                    onClick = { onSelectMode(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, TriggerType.entries.size),
                    enabled = isConnected,
                    icon = {}
                ) {
                    Text(stringResource(type.label))
                }
            }
        }

        Box(modifier = Modifier.height(72.dp), contentAlignment = Alignment.Center) {
            val hint = when (triggerType) {
                TriggerType.Direct -> R.string.direct_hint
                TriggerType.Countdown -> if (countdownRunning) R.string.countdown_running_hint else null
                TriggerType.Bulb -> if (isBulbActive) R.string.bulb_hint_on else R.string.bulb_hint_off
            }
            if (hint != null) {
                Text(
                    text = stringResource(hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                DurationPicker(countdownDuration, onDurationChange)
            }
        }

        val active = countdownRunning || isBulbActive
        val label = when (triggerType) {
            TriggerType.Direct -> R.string.shutter
            TriggerType.Countdown -> if (countdownRunning) R.string.stop else R.string.start
            TriggerType.Bulb -> if (isBulbActive) R.string.close else R.string.open
        }
        ShutterButton(active = active, enabled = isConnected, label = stringResource(label), onClick = onShutter)
    }
}

private val TriggerType.label
    get() = when (this) {
        TriggerType.Direct -> R.string.mode_direct
        TriggerType.Countdown -> R.string.mode_countdown
        TriggerType.Bulb -> R.string.mode_bulb
    }

@Composable
private fun DurationPicker(countdownDuration: Int, onDurationChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.countdown_delay),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(durationLabel(countdownDuration)) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                durationOptions.forEach { seconds ->
                    DropdownMenuItem(
                        text = { Text(durationLabel(seconds)) },
                        onClick = {
                            onDurationChange(seconds)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun durationLabel(seconds: Int): String =
    if (seconds % 60 == 0) stringResource(R.string.minutes_short, seconds / 60)
    else stringResource(R.string.seconds_short, seconds)

/** A large round button styled like a physical shutter release. Orange while something is running. */
@Composable
private fun ShutterButton(active: Boolean, enabled: Boolean, label: String, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val color = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        active -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(3.dp, color, CircleShape)
                .padding(7.dp)
                .clip(CircleShape)
                .background(color)
                .semantics { contentDescription = label }
                .clickable(enabled = enabled, role = Role.Button) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
