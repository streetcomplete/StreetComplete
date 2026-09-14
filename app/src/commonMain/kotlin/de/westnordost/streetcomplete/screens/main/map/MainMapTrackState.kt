package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.units.International

@Composable
fun rememberMainMapTrackState(): MainMapTrackState = rememberSaveable(saver = MainMapTrackState.Saver) {
    MainMapTrackState()
}

/** Walked and recorded tracks, owned by the map screen's composition. */
class MainMapTrackState {
    private val tracks = mutableStateListOf(mutableStateListOf<Trackpoint>())

    var isRecording by mutableStateOf(false)
        private set

    val currentTrack: List<Trackpoint> get() = tracks.last().toList()
    val previousTracks: List<List<Trackpoint>> get() = tracks.dropLast(1).map { it.toList() }

    /** Positions of [currentTrack], recomputed only when the track changes */
    val currentTrackPositions: List<LatLon> by derivedStateOf { tracks.last().map { it.position } }
    /** Positions of [previousTracks], recomputed only when the tracks change */
    val previousTrackPositions: List<List<LatLon>> by derivedStateOf {
        tracks.dropLast(1).map { track -> track.map { it.position } }
    }

    var recordedTrack: List<Trackpoint> by mutableStateOf(emptyList())
        private set

    fun addLocation(location: LocationMeasurement) {
        val accuracy = location.horizontalAccuracy?.toFloat(International.Meters) ?: 0f
        if (accuracy > 20f) return
        val time = location.measuredAt.toEpochMilliseconds()
        val lastLocation = tracks.last().lastOrNull()
        if (!isRecording && lastLocation != null && time - lastLocation.time > 60_000) {
            tracks.add(mutableStateListOf())
        }
        tracks.last().add(Trackpoint(
            position = location.position.toLatLon(),
            time = time,
            accuracy = accuracy,
            elevation = location.position.altitude?.toFloat() ?: 0f,
        ))
    }

    fun startRecording() {
        isRecording = true
        recordedTrack = emptyList()
        tracks.add(mutableStateListOf())
    }

    fun stopRecording() {
        isRecording = false
        recordedTrack = currentTrack
        tracks.add(mutableStateListOf())
    }

    /** Losing location clears visible tracks but does not stop recording. */
    fun clear() {
        tracks.clear()
        tracks.add(mutableStateListOf())
    }

    companion object {
        val Saver = Saver<MainMapTrackState, String>(
            save = { state ->
                // Instance state is limited to about 1 MB per transaction, so only the newest
                // points are saved. A recording longer than this is cut on process death.
                var remaining = MAX_SAVED_TRACKPOINTS
                val tracks = state.tracks.asReversed().map { track ->
                    track.takeLast(remaining).also { remaining -= it.size }
                }.asReversed().dropWhile { it.isEmpty() }.ifEmpty { listOf(emptyList()) }
                Json.encodeToString(SavedTracks(tracks, state.isRecording))
            },
            restore = { value ->
                val saved = Json.decodeFromString<SavedTracks>(value)
                MainMapTrackState().apply {
                    tracks.clear()
                    tracks.addAll(saved.tracks.map { it.toMutableStateList() })
                    isRecording = saved.isRecording
                }
            },
        )
    }
}

internal const val MAX_SAVED_TRACKPOINTS = 1000

@Serializable
private data class SavedTracks(val tracks: List<List<Trackpoint>>, val isRecording: Boolean)
