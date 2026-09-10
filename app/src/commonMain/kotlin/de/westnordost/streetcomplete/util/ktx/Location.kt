package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.maplibre.compose.location.LocationBackendAvailability
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.spatialk.units.International
import kotlin.time.TimeSource

private val locationTimeMark = TimeSource.Monotonic.markNow()

fun LocationEvent.Update.toLocation(): Location =
    Location(
        position = measurement.position.toLatLon(),
        accuracy = measurement.horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
        elapsedDuration = locationTimeMark.elapsedNow() - measurementMark.elapsedNow(),
    )

fun org.maplibre.spatialk.geojson.Position.toLatLon(): LatLon =
    LatLon(latitude, longitude)

/** Restart a foreground request when permission changes, without requesting permission implicitly. */
@OptIn(ExperimentalCoroutinesApi::class)
fun LocationProvider.updatesWithPermission(
    request: LocationRequest = LocationRequest(),
): Flow<LocationEvent> {
    val availability = backendAvailability
    if (availability != LocationBackendAvailability.Available) {
        return flowOf(LocationEvent.Unavailable(
            reason = if (availability is LocationBackendAvailability.Unsupported) {
                LocationUnavailableReason.Unsupported
            } else {
                LocationUnavailableReason.UnexpectedFailure
            },
            cause = (availability as? LocationBackendAvailability.Misconfigured)?.cause,
        ))
    }
    return permission.flatMapLatest { updates(request) }
}
