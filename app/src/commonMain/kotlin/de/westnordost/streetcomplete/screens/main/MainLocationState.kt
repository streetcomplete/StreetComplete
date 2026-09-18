package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.maplibre.compose.location.HeadingMeasurement
import org.maplibre.compose.location.HeadingProvider
import org.maplibre.compose.location.HeadingRequest
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberMainLocationState(
    locationProvider: LocationProvider,
    headingProvider: HeadingProvider,
): MainLocationState {
    val location = rememberSerializable { mutableStateOf<LocationMeasurement?>(null) }
    return remember(locationProvider, headingProvider) {
        MainLocationState(locationProvider, headingProvider, location)
    }
}

/** The user's location and heading and the state of the location button */
@Stable
class MainLocationState internal constructor(
    private val locationProvider: LocationProvider,
    private val headingProvider: HeadingProvider,
    location: MutableState<LocationMeasurement?>,
) {
    /** The last location, saved so it is shown again immediately after process death */
    var location by location
        private set

    private var heading by mutableStateOf<HeadingMeasurement?>(null)

    /** State displayed by the location button */
    var state by mutableStateOf<LocationState?>(LocationState.ENABLED)
        private set

    val position: LatLon? get() = location?.position?.toLatLon()

    /** Compass heading in degrees, clockwise from north */
    val headingDegrees: Float? get() = heading?.let { (it.bearing - Bearing.North).inDegrees.toFloat() }

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
