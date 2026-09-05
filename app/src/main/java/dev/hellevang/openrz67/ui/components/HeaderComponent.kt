package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.hellevang.openrz67.R
import androidx.compose.ui.text.style.TextAlign
import dev.hellevang.openrz67.ui.theme.Dimens

@Composable
fun HeaderComponent() {
    Text(
        text = stringResource(R.string.header_title),
        fontSize = Dimens.HeaderTextSize,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(start = Dimens.HeaderStartPadding, top = Dimens.HeaderTopPadding)
    )
    Text(
        text = stringResource(R.string.header_subtitle),
        fontSize = Dimens.SubHeaderTextSize,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.StandardPadding, end = Dimens.StandardPadding)
    )
}