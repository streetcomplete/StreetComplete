package de.westnordost.streetcomplete.screens.main.map

import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class MainMapTrackStateTest {
    @Test fun `ignores imprecise locations but accepts unknown accuracy`() {
        val state = MainMapTrackState()
        state.addLocation(location(0).copy(horizontalAccuracy = 21.meters))
        state.addLocation(location(1).copy(horizontalAccuracy = 20.meters))
        state.addLocation(location(2))

        assertEquals(listOf(20f, 0f), state.currentTrack.map { it.accuracy })
    }

    @Test fun `splits walked tracks after a minute gap`() {
        val state = MainMapTrackState()
        state.addLocation(location(0))
        state.addLocation(location(60_000))
        state.addLocation(location(120_001))

        assertEquals(listOf(0L, 60_000L), state.previousTracks.single().map { it.time })
        assertEquals(listOf(120_001L), state.currentTrack.map { it.time })
    }

    @Test fun `recording spans gaps and retains its points when stopped`() {
        val state = MainMapTrackState()
        state.addLocation(location(0))
        state.startRecording()
        state.addLocation(location(1))
        state.addLocation(location(120_000))
        state.stopRecording()
        state.addLocation(location(120_001))

        assertEquals(listOf(1L, 120_000L), state.recordedTrack.map { it.time })
        assertEquals(listOf(120_001L), state.currentTrack.map { it.time })
    }

    @Test fun `losing location clears tracks without stopping recording`() {
        val state = MainMapTrackState()
        state.startRecording()
        state.addLocation(location(0))
        state.clear()

        assertTrue(state.isRecording)
        assertTrue(state.currentTrack.isEmpty())
        assertTrue(state.previousTracks.isEmpty())
    }

    private fun location(time: Long) = LocationMeasurement(
        position = Position(1.0, 2.0),
        measuredAt = Instant.fromEpochMilliseconds(time),
    )
}
