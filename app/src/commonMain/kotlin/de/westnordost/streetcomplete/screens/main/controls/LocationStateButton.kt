package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_location_24
import de.westnordost.streetcomplete.resources.ic_location_disabled_24
import de.westnordost.streetcomplete.resources.ic_location_navigation_24
import de.westnordost.streetcomplete.resources.ic_location_navigation_no_location_24
import de.westnordost.streetcomplete.resources.ic_location_no_location_24
import de.westnordost.streetcomplete.resources.map_btn_gps_tracking
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** State of location updates */
enum class LocationState {
    /** user declined to give this app access to location */
    DENIED,
    /** user allowed this app to access location (but location disabled) */
    ALLOWED,
    /** location service is turned on (but no location request active) */
    ENABLED,
    /** requested location updates and waiting for first fix */
    SEARCHING,
    /** receiving location updates */
    UPDATING;

    val isEnabled: Boolean get() = ordinal >= ENABLED.ordinal
}

/** Map button that shows the current state of location updates and map mode */
@Composable
fun LocationStateButton(
    onClick: () -> Unit,
    state: LocationState,
    modifier: Modifier = Modifier,
    isNavigationMode: Boolean = false,
    isFollowing: Boolean = false,
    enabled: Boolean = true
) {
    var iconResource by remember(state, isNavigationMode) {
        mutableStateOf(getIcon(state, isNavigationMode))
    }

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
            contentDescription = stringResource(Res.string.map_btn_gps_tracking),
            tint = if (isFollowing) MaterialTheme.colors.secondary else LocalContentColor.current
        )
    }
}

private fun getIcon(state: LocationState, isNavigationMode: Boolean) = when (state) {
    LocationState.DENIED,
    LocationState.ALLOWED ->
        Res.drawable.ic_location_disabled_24
    LocationState.ENABLED,
    LocationState.SEARCHING ->
        if (isNavigationMode) Res.drawable.ic_location_navigation_no_location_24
        else Res.drawable.ic_location_no_location_24
    LocationState.UPDATING ->
        if (isNavigationMode) Res.drawable.ic_location_navigation_24
        else Res.drawable.ic_location_24
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
