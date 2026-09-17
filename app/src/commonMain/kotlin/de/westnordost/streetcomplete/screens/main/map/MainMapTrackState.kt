package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
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
    private val recentPositions = mutableStateListOf<LatLon>()
    private val olderPositions = mutableStateListOf<List<LatLon>>()

    var isRecording by mutableStateOf(false)
        private set

    val currentTrack: List<Trackpoint> get() = tracks.last().toList()

    /** Only this short tail needs to be sent to the map on each location update. */
    val recentTrackPositions: List<LatLon> get() = recentPositions.toList()
    /** Completed tracks and batches, updated only when the recent tail rolls over. */
    val olderTrackPositions: List<List<LatLon>> get() = olderPositions.toList()

    var recordedTrack: List<Trackpoint> by mutableStateOf(emptyList())
        private set

    fun addLocation(location: LocationMeasurement) {
        val accuracy = location.horizontalAccuracy?.toFloat(International.Meters) ?: 0f
        if (accuracy > 20f) return
        val time = location.measuredAt.toEpochMilliseconds()
        val lastLocation = tracks.last().lastOrNull()
        if (!isRecording && lastLocation != null && time - lastLocation.time > 60_000) {
            startNewTrack()
        }
        val position = location.position.toLatLon()
        tracks.last().add(Trackpoint(
            position = position,
            time = time,
            accuracy = accuracy,
            elevation = location.position.altitude?.toFloat() ?: 0f,
        ))
        addDisplayPosition(position)
    }

    fun startRecording() {
        isRecording = true
        recordedTrack = emptyList()
        startNewTrack()
    }

    fun stopRecording() {
        isRecording = false
        recordedTrack = currentTrack
        startNewTrack()
    }

    /** Losing location clears visible tracks but does not stop recording. */
    fun clear() {
        tracks.clear()
        tracks.add(mutableStateListOf())
        recentPositions.clear()
        olderPositions.clear()
    }

    private fun startNewTrack() {
        if (recentPositions.isNotEmpty()) olderPositions.add(recentPositions.toList())
        recentPositions.clear()
        tracks.add(mutableStateListOf())
    }

    private fun addDisplayPosition(position: LatLon) {
        recentPositions.add(position)
        if (recentPositions.size > MAX_RECENT_TRACKPOINTS) {
            // Share the boundary point so there is no gap between the two lines.
            olderPositions.add(recentPositions.take(TRACKPOINT_BATCH_SIZE + 1))
            recentPositions.subList(0, TRACKPOINT_BATCH_SIZE).clear()
        }
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
                    olderPositions.addAll(saved.tracks.dropLast(1).map { track ->
                        track.map { it.position }
                    })
                    saved.tracks.last().forEach { addDisplayPosition(it.position) }
                }
            },
        )
    }
}

private const val MAX_RECENT_TRACKPOINTS = 100
private const val TRACKPOINT_BATCH_SIZE = 50
internal const val MAX_SAVED_TRACKPOINTS = 1000

@Serializable
private data class SavedTracks(val tracks: List<List<Trackpoint>>, val isRecording: Boolean)
