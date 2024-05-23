package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R

@Composable
fun BackIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_back_24dp),
        contentDescription = stringResource(R.string.action_back),
    )
}

@Composable
fun ClearIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_clear_24dp),
        contentDescription = stringResource(R.string.action_clear),
    )
}
