package de.westnordost.streetcomplete.screens.main.map

import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class MainMapTrackStateTest {
    @Test fun preciseMeasurementRetainsWallTimeAccuracyAndElevation() {
        val state = MainMapTrackState()

        state.onLocationMeasurement(measurement(1, accuracy = 4.5, altitude = 12.25))

        val point = state.currentTrack.single()
        assertEquals(1_000L, point.time)
        assertEquals(4.5f, point.accuracy)
        assertEquals(12.25f, point.elevation)
        assertEquals(1, state.currentRenderedTrack.size)
    }

    @Test fun inaccurateMeasurementIsDisplayedButNotTracked() {
        val state = MainMapTrackState()
        val measurement = measurement(1, accuracy = 20.1)

        state.onLocationMeasurement(measurement)

        assertEquals(measurement, state.displayedMeasurement)
        assertTrue(state.currentTrack.isEmpty())
    }

    @Test fun nonRecordingGapStartsNewSegmentButRecordingGapDoesNot() {
        val state = MainMapTrackState()
        state.onLocationMeasurement(measurement(0))
        state.onLocationMeasurement(measurement(61))
        assertEquals(1, state.oldRenderedTracks.size)
        assertEquals(1, state.currentRenderedTrack.size)

        state.startRecording()
        state.onLocationMeasurement(measurement(62))
        state.onLocationMeasurement(measurement(180))
        assertEquals(2, state.currentTrack.size)
    }

    @Test fun rendererMovesFiftyPointsAtATimeOutOfHotTrack() {
        val state = MainMapTrackState()

        repeat(101) { state.onLocationMeasurement(measurement(it)) }

        assertEquals(51, state.currentRenderedTrack.size)
        assertEquals(50, state.oldRenderedTracks.last().size)
        assertEquals(101, state.currentTrack.size)
    }

    @Test fun recordingHandoffAndUnavailableStatePreserveCaptureIntent() {
        val state = MainMapTrackState()
        state.startRecording()
        state.onLocationMeasurement(measurement(1))
        state.onLocationMeasurement(measurement(2))

        val recorded = state.stopRecording()
        assertEquals(2, recorded.size)
        assertFalse(state.isRecording)
        assertTrue(state.currentTrack.isEmpty())

        state.startRecording()
        state.onLocationMeasurement(measurement(3))
        state.onLocationUnavailable()
        assertTrue(state.isRecording)
        assertTrue(state.currentTrack.isEmpty())
        assertTrue(state.currentRenderedTrack.isEmpty())
    }

    @Test fun savedSnapshotKeepsOnlyLastThousandPointsAndRestoresSegments() {
        val state = MainMapTrackState()
        repeat(1_005) { state.onLocationMeasurement(measurement(it)) }

        val snapshot = state.snapshot()
        val restored = MainMapTrackState(
            Json.decodeFromString(Json.encodeToString(snapshot))
        )

        assertEquals(1_000, snapshot.tracks.sumOf { it.size })
        assertEquals(1_000, restored.currentTrack.size)
        assertTrue(restored.currentRenderedTrack.size <= 100)
        assertEquals(snapshot.displayedMeasurement, restored.displayedMeasurement)
    }

    private fun measurement(
        seconds: Int,
        accuracy: Double = 5.0,
        altitude: Double? = null,
    ) = LocationMeasurement(
        position = Position(
            longitude = seconds / 10_000.0,
            latitude = seconds / 20_000.0,
            altitude = altitude,
        ),
        horizontalAccuracy = accuracy.meters,
        measuredAt = Instant.fromEpochMilliseconds(seconds * 1_000L),
    )
}
