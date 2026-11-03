package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.screens.main.MainSheetSelection
import io.github.vinceglb.filekit.FileKit
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class MainMapTrackStateTest {

    private val testSaverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    private fun <T, S : Any> Saver<T, S>.testSave(value: T): S? =
        with(testSaverScope) { save(value) }

    @BeforeTest
    fun setup() {
        try {
            FileKit.init(appId = "de.westnordost.streetcomplete.test")
        } catch (ignored: Exception) {
        }
        cleanup()
    }

    @AfterTest
    fun cleanup() {
        MainMapTrackState.deleteTracksCache()
        MainSheetSelection.deleteDraftNoteTrackCache()
    }

    @Test
    fun `saving and restoring 1,001 trackpoints preserves all 1,001 points starting from timestamp 0`() {
        val state = MainMapTrackState()
        state.startRecording()
        for (i in 0..1000) {
            state.addLocation(
                LocationMeasurement(
                    position = Position(longitude = 10.0, latitude = 50.0),
                    measuredAt = Instant.fromEpochMilliseconds(i.toLong()),
                    horizontalAccuracy = 5.meters,
                )
            )
        }
        assertEquals(1001, state.currentTrack.size)

        val saved = MainMapTrackState.Saver.testSave(state)
        assertNotNull(saved)

        val restored = MainMapTrackState.Saver.restore(saved)
        assertNotNull(restored)
        assertTrue(restored.isRecording)
        assertEquals(1001, restored.currentTrack.size)
        assertEquals(0L, restored.currentTrack.first().time)
        assertEquals(1000L, restored.currentTrack.last().time)
        assertEquals(
            (0L..1000L).toList(),
            restored.currentTrack.map { it.time },
        )
    }

    @Test
    fun `saving and restoring CreateNote with 1,001 trackpoints preserves all points`() {
        val points = (0..1000).map { i ->
            Trackpoint(
                position = LatLon(50.0, 10.0),
                time = i.toLong(),
                accuracy = 5f,
                elevation = 100f,
            )
        }
        val noteSelection = MainSheetSelection.CreateNote(LatLon(50.0, 10.0), points)

        val saved = MainSheetSelection.Saver.testSave(noteSelection)
        assertNotNull(saved)

        val restored = MainSheetSelection.Saver.restore(saved) as? MainSheetSelection.CreateNote
        assertNotNull(restored)
        assertNotNull(restored.trackpoints)
        val restoredPoints = restored.trackpoints
        assertEquals(1001, restoredPoints.size)
        assertEquals(0L, restoredPoints.first().time)
        assertEquals(1000L, restoredPoints.last().time)
        assertEquals(
            (0L..1000L).toList(),
            restoredPoints.map { it.time },
        )
    }

    @Test
    fun `ignores imprecise locations but accepts unknown accuracy`() {
        val state = MainMapTrackState()
        state.addLocation(location(0).copy(horizontalAccuracy = 21.meters))
        state.addLocation(location(1).copy(horizontalAccuracy = 20.meters))
        state.addLocation(location(2))

        assertEquals(listOf(20f, 0f), state.currentTrack.map { it.accuracy })
    }

    @Test
    fun `stopRecording clears the cache file`() {
        val state = MainMapTrackState()
        state.startRecording()
        state.addLocation(location(0))
        MainMapTrackState.Saver.testSave(state)

        val recorded = state.stopRecording()
        assertEquals(1, recorded.size)

        // After stopRecording, a subsequent save has empty tracks and cache is removed
        val saved = MainMapTrackState.Saver.testSave(state)
        val restored = MainMapTrackState.Saver.restore(saved!!)
        assertNotNull(restored)
        assertTrue(restored.currentTrack.isEmpty())
    }

    @Test
    fun `clear resets tracks and deletes cache`() {
        val state = MainMapTrackState()
        state.startRecording()
        state.addLocation(location(0))
        MainMapTrackState.Saver.testSave(state)

        state.clear()
        assertTrue(state.isRecording)
        assertTrue(state.currentTrack.isEmpty())
    }

    private fun location(time: Long) = LocationMeasurement(
        position = Position(longitude = 2.0, latitude = 1.0),
        measuredAt = Instant.fromEpochMilliseconds(time),
    )
}
