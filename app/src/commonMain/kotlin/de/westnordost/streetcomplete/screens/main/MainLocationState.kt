package de.westnordost.streetcomplete.screens.main

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.no_location_permission_warning
import de.westnordost.streetcomplete.resources.no_location_permission_warning_title
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.turn_on_location_request
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.location.HeadingMeasurement
import org.maplibre.compose.location.HeadingProvider
import org.maplibre.compose.location.HeadingRequest
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberMainLocationState(
    locationProvider: LocationProvider,
    headingProvider: HeadingProvider,
    systemSettingsLauncher: SystemSettingsLauncher,
): MainLocationState {
    val location = rememberSerializable { mutableStateOf<LocationMeasurement?>(null) }
    return remember(locationProvider, headingProvider, systemSettingsLauncher) {
        MainLocationState(locationProvider, headingProvider, systemSettingsLauncher, location)
    }
}

/** The user's location and heading, the state of the location button and the dialogs that ask
 *  the user to grant location permission or to enable location services. */
@Stable
class MainLocationState internal constructor(
    private val locationProvider: LocationProvider,
    private val headingProvider: HeadingProvider,
    private val systemSettingsLauncher: SystemSettingsLauncher,
    location: MutableState<LocationMeasurement?>,
) {
    /** The last location, saved so it is shown again immediately after process death */
    var location by location
        private set

    var heading by mutableStateOf<HeadingMeasurement?>(null)
        private set

    /** State displayed by the location button */
    var state by mutableStateOf<LocationState?>(LocationState.ENABLED)
        private set

    var dialog by mutableStateOf<LocationDialog?>(null)
        private set

    val position: LatLon? get() = location?.position?.toLatLon()

    /** Compass heading in degrees, clockwise from north */
    val headingDegrees: Float? get() = heading?.let { (it.bearing - Bearing.North).inDegrees.toFloat() }

    /** Asks for permission or to enable location services if necessary. Returns whether the
     *  location is available. Calls [onCannotEnable] if it is not and there is no way to
     *  enable it from here. */
    fun requestLocation(onCannotEnable: () -> Unit): Boolean {
        val permission = locationProvider.permission.value
        if (permission is LocationPermission.NotGranted) {
            when {
                permission.canRequest == false -> {
                    if (systemSettingsLauncher.canOpenApplicationSettings) dialog = LocationDialog.ApplicationSettings
                    else onCannotEnable()
                }
                permission.shouldShowRationale -> dialog = LocationDialog.PermissionRationale
                else -> locationProvider.requestPermission()
            }
            return false
        }
        if (state == LocationState.ALLOWED) {
            if (systemSettingsLauncher.canOpenLocationServicesSettings) dialog = LocationDialog.LocationSettings
            else onCannotEnable()
            return false
        }
        return true
    }

    fun confirmDialog() {
        when (dialog) {
            LocationDialog.PermissionRationale -> locationProvider.requestPermission()
            LocationDialog.ApplicationSettings -> systemSettingsLauncher.openApplicationSettings()
            LocationDialog.LocationSettings -> systemSettingsLauncher.openLocationServicesSettings()
            null -> {}
        }
        dialog = null
    }

    fun dismissDialog() {
        dialog = null
    }

    /** Collects location and heading updates until cancelled. Each location event is reported to
     *  [onEvent] after this state has been updated. */
    suspend fun collectUpdates(onEvent: (LocationEvent) -> Unit): Unit = coroutineScope {
        launch {
            locationProvider.updates(LocationRequest()).collect { event ->
                this@MainLocationState.onEvent(event)
                onEvent(event)
            }
        }
        launch {
            headingProvider.updates(HeadingRequest(33.milliseconds)).collect { heading = it }
        }
    }

    private fun onEvent(event: LocationEvent) {
        when (event) {
            is LocationEvent.Update -> {
                location = event.measurement
                state = LocationState.UPDATING
            }
            is LocationEvent.Unavailable -> {
                location = null
                state = when (event.reason) {
                    LocationUnavailableReason.ServicesDisabled -> LocationState.ALLOWED
                    LocationUnavailableReason.TemporarilyUnavailable -> LocationState.SEARCHING
                    LocationUnavailableReason.PermissionDenied -> LocationState.DENIED
                    LocationUnavailableReason.Unsupported, LocationUnavailableReason.UnexpectedFailure -> null
                }
            }
        }
    }
}

enum class LocationDialog { PermissionRationale, ApplicationSettings, LocationSettings }

@Composable
fun LocationDialog(dialog: LocationDialog, onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = if (dialog == LocationDialog.PermissionRationale) {
            { Text(stringResource(Res.string.no_location_permission_warning_title)) }
        } else null,
        text = { Text(stringResource(if (dialog == LocationDialog.PermissionRationale)
            Res.string.no_location_permission_warning else Res.string.turn_on_location_request)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.cancel)) } },
    )
}
