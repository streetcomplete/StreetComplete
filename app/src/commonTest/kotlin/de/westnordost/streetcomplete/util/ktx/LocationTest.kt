package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.maplibre.compose.location.LocationAccuracyAuthorization
import org.maplibre.compose.location.LocationBackendAvailability
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
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TestTimeSource

class LocationTest {
    private val measurement = LocationMeasurement(
        position = Position(13.0, 52.0, 45.0),
        horizontalAccuracy = 7.meters,
        measuredAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
    )

    @Test fun `survey age uses the monotonic measurement mark`() {
        val time = TestTimeSource()
        val event = LocationEvent.Update(measurement, time.markNow())
        time += 12.seconds
        val location = event.toLocation()
        assertEquals(12.seconds, location.elapsedDuration)
        assertEquals(LatLon(52.0, 13.0), location.position)
        assertEquals(7f, location.accuracy)
    }

    @Test fun `restored measurements retain absolute track timestamps`() {
        val restored = Json.decodeFromString<LocationMeasurement>(Json.encodeToString(measurement))
        val first = restored.toTrackpoint()
        val next = restored.copy(measuredAt = restored.measuredAt + 30.seconds).toTrackpoint()
        assertEquals(1_700_000_000_000L, first.time)
        assertEquals(30_000L, next.time - first.time)
        assertEquals(LatLon(52.0, 13.0), first.position)
        assertEquals(7f, first.accuracy)
        assertEquals(45f, first.elevation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun `permission changes restart and cancellation stops the request`() = runTest {
        val permission = MutableStateFlow<LocationPermission>(LocationPermission.NotGranted(true))
        var starts = 0
        var stops = 0
        val request = LocationRequest(minimumInterval = 20.seconds)
        val provider = object : LocationProvider {
            override val permission = permission
            override fun requestPermission() = error("Must not prompt implicitly")
            override fun updates(request: LocationRequest): Flow<LocationEvent> = flow {
                assertEquals(20.seconds, request.minimumInterval)
                starts++
                try { awaitCancellation() } finally { stops++ }
            }
        }
        val job = launch { provider.updatesWithPermission(request).collect {} }
        runCurrent()
        assertEquals(1, starts)
        permission.value = LocationPermission.Granted(LocationAccuracyAuthorization.Precise)
        runCurrent()
        assertEquals(2, starts)
        assertEquals(1, stops)
        job.cancel()
        runCurrent()
        assertEquals(2, stops)
    }

    @Test fun `unavailable backend is reported without starting a request`() = runTest {
        val cause = IllegalStateException("Backend setup failed")
        val provider = object : LocationProvider {
            override val backendAvailability = LocationBackendAvailability.Misconfigured(cause)
            override fun updates(request: LocationRequest): Flow<LocationEvent> = error("Unavailable")
        }
        val event = provider.updatesWithPermission().first() as LocationEvent.Unavailable
        assertEquals(LocationUnavailableReason.UnexpectedFailure, event.reason)
        assertSame(cause, event.cause)
    }
}
