package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R
import kotlinx.coroutines.delay

@Composable
fun LocationStateButton(
    onClick: () -> Unit,
    state: LocationState,
    modifier: Modifier = Modifier,
    isNavigationMode: Boolean = false,
    isFollowing: Boolean = false,
    enabled: Boolean = true
) {
    var iconResource by remember(state) { mutableStateOf(getIcon(state, isNavigationMode)) }

    MapButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        LaunchedEffect(state) {
            if (state == LocationState.SEARCHING) {
                while (true) {
                    delay(750)
                    iconResource = getIcon(LocationState.UPDATING, isNavigationMode)
                    delay(750)
                    iconResource = getIcon(LocationState.ENABLED, isNavigationMode)
                }
            }
        }
        Icon(
            painter = painterResource(iconResource),
            contentDescription = stringResource(R.string.map_btn_gps_tracking),
            tint = if (isFollowing) MaterialTheme.colors.secondary else Color.Black
        )
    }
}

private fun getIcon(state: LocationState, isNavigationMode: Boolean) = when (state) {
    LocationState.DENIED,
    LocationState.ALLOWED ->
        R.drawable.ic_location_disabled_24dp
    LocationState.ENABLED,
    LocationState.SEARCHING ->
        if (isNavigationMode) R.drawable.ic_location_navigation_no_location_24dp
        else R.drawable.ic_location_no_location_24dp
    LocationState.UPDATING ->
        if (isNavigationMode) R.drawable.ic_location_navigation_24dp
        else R.drawable.ic_location_24dp
}

@Preview
@Composable
private fun PreviewLocationButton() {
    Column {
        for (state in LocationState.entries) {
            Row {
                LocationStateButton(onClick = {}, state = state)
                LocationStateButton(onClick = {}, state = state, isNavigationMode = true)
                LocationStateButton(onClick = {}, state = state, isFollowing = true)
                LocationStateButton(onClick = {}, state = state, isNavigationMode = true, isFollowing = true)
            }
        }
    }
}
