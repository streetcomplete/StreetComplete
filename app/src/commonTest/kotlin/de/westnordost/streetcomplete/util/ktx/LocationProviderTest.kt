package de.westnordost.streetcomplete.util.ktx

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.maplibre.compose.location.LocationAccuracyAuthorization
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource

class LocationProviderTest {
    @Test fun `permission changes restart updates without requesting permission`() = runBlocking {
        withTimeout(5.seconds) {
            val denied = LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied)
            val update = LocationEvent.Update(
                LocationMeasurement(Position(2.0, 1.0), measuredAt = Instant.fromEpochSeconds(100)),
                TimeSource.Monotonic.markNow(),
            )
            val requests = mutableListOf<LocationRequest>()
            var activeRequests = 0
            val provider = object : LocationProvider {
                override val permission = MutableStateFlow<LocationPermission>(LocationPermission.NotGranted(true))

                override fun requestPermission() = error("Permission prompts must remain explicit")

                override fun updates(request: LocationRequest) = flow {
                    requests.add(request)
                    if (permission.value !is LocationPermission.Granted) {
                        emit(denied)
                        return@flow
                    }
                    activeRequests++
                    try {
                        emit(update)
                        awaitCancellation()
                    } finally {
                        activeRequests--
                    }
                }
            }
            val events = Channel<LocationEvent>(Channel.UNLIMITED)
            val request = LocationRequest(minimumInterval = 30.seconds, minimumDistance = 100.meters)
            val job = launch {
                provider.updatesWithPermissionChanges(request).collect { events.send(it) }
            }
            try {
                assertEquals(denied, events.receive())
                provider.permission.value = LocationPermission.Granted(LocationAccuracyAuthorization.Precise)
                assertEquals(update, events.receive())
                assertEquals(1, activeRequests)

                provider.permission.value = LocationPermission.NotGranted(true)
                assertEquals(denied, events.receive())
                assertEquals(0, activeRequests)

                provider.permission.value = LocationPermission.Granted(LocationAccuracyAuthorization.Precise)
                assertEquals(update, events.receive())
                assertEquals(1, activeRequests)
                assertEquals(List(4) { request }, requests)
            } finally {
                job.cancel()
                job.join()
            }
            assertEquals(0, activeRequests)
        }
    }
}
