package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.hellevang.openrz67.R
import androidx.compose.ui.unit.dp
import dev.hellevang.openrz67.ui.theme.Dimens

@Composable
fun CountdownDisplay(
    startDelayedTrigger: Boolean,
    countdownTimeLeft: Int,
    countdownDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val durationOptions = listOf(2, 5, 10, 15, 30, 60, 120, 180, 240)
    if (startDelayedTrigger) {
        if (countdownTimeLeft > 0) {
            Text(
                fontSize = Dimens.CountdownTextSize,
                color = MaterialTheme.colors.secondary,
                text = "$countdownTimeLeft"
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontSize = Dimens.BodyTextSize,
                color = MaterialTheme.colors.onBackground,
                text = stringResource(R.string.countdown_prefix)
            )
            
            Box {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(durationLabel(countdownDuration))
                }
                
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    durationOptions.forEach { seconds ->
                        DropdownMenuItem(
                            onClick = {
                                onDurationChange(seconds)
                                dropdownExpanded = false
                            }
                        ) {
                            Text(durationLabel(seconds))
                        }
                    }
                }
            }
            
            Text(
                fontSize = Dimens.BodyTextSize,
                color = MaterialTheme.colors.onBackground,
                text = stringResource(R.string.countdown_suffix)
            )
        }
    }
}
@Composable
private fun durationLabel(seconds: Int): String =
    if (seconds % 60 == 0) stringResource(R.string.minutes_short, seconds / 60)
    else stringResource(R.string.seconds_short, seconds)
