package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.units.International
import kotlin.math.max

/** Walked and recorded tracks, owned by the application composition above navigation. */
class MainMapTrackState {
    private class Track(val points: SnapshotStateList<Trackpoint>, val isRecording: Boolean) {
        /** Points before this index are drawn as part of the older tracks. A recording is never
         *  cut so that it is drawn completely in the recording color. */
        val cutIndex: Int get() =
            if (isRecording) 0 else max(0, (points.size / TRACKPOINT_BATCH_SIZE - 1) * TRACKPOINT_BATCH_SIZE)
    }

    private val tracks = mutableStateListOf(Track(mutableStateListOf(), isRecording = false))

    val isRecording: Boolean get() = tracks.last().isRecording

    val currentTrack: List<Trackpoint> get() = tracks.last().points.toList()

    /** Only this tail of the current track needs to be sent to the map on each location update. */
    val recentTrackPositions: List<LatLon> get() {
        val track = tracks.last()
        return track.points.subList(track.cutIndex, track.points.size).map { it.position }
    }

    /** Completed tracks and the older part of the current track. This changes only every
     *  [TRACKPOINT_BATCH_SIZE] points, so the map does not need to convert all points each time. */
    val olderTrackPositions: List<List<LatLon>> get() {
        val current = tracks.last()
        val older = tracks.dropLast(1).map { track -> track.points.map { it.position } }
        val cut = current.cutIndex
        if (cut == 0) return older
        // Share the boundary point so there is no gap between the two lines.
        return older + listOf(current.points.subList(0, cut + 1).map { it.position })
    }

    fun addLocation(location: LocationMeasurement) {
        val accuracy = location.horizontalAccuracy?.toFloat(International.Meters) ?: 0f
        if (accuracy > 20f) return
        val time = location.measuredAt.toEpochMilliseconds()
        val lastLocation = tracks.last().points.lastOrNull()
        if (!isRecording && lastLocation != null && time - lastLocation.time > 60_000) {
            startNewTrack(isRecording = false)
        }
        tracks.last().points.add(Trackpoint(
            position = location.position.toLatLon(),
            time = time,
            accuracy = accuracy,
            elevation = location.position.altitude?.toFloat() ?: 0f,
        ))
    }

    fun startRecording() {
        startNewTrack(isRecording = true)
    }

    /** Stops recording and returns the recorded track */
    fun stopRecording(): List<Trackpoint> {
        val recorded = currentTrack
        startNewTrack(isRecording = false)
        return recorded
    }

    /** Losing location clears visible tracks but does not stop recording. */
    fun clear() {
        val isRecording = isRecording
        tracks.clear()
        tracks.add(Track(mutableStateListOf(), isRecording))
    }

    private fun startNewTrack(isRecording: Boolean) {
        tracks.add(Track(mutableStateListOf(), isRecording))
    }

    companion object {
        val Saver = Saver<MainMapTrackState, String>(
            save = { state ->
                // Instance state is limited to about 1 MB per transaction, so only the newest
                // points are saved. A recording longer than this is cut on process death.
                var remaining = MAX_SAVED_TRACKPOINTS
                val tracks = state.tracks.asReversed().map { track ->
                    val points = track.points.takeLast(remaining).also { remaining -= it.size }
                    SavedTrack(points, track.isRecording)
                }.asReversed().dropWhile { it.points.isEmpty() }
                    .ifEmpty { listOf(SavedTrack(emptyList(), state.isRecording)) }
                Json.encodeToString(tracks)
            },
            restore = { value ->
                MainMapTrackState().apply {
                    tracks.clear()
                    tracks.addAll(Json.decodeFromString<List<SavedTrack>>(value).map {
                        Track(it.points.toMutableStateList(), it.isRecording)
                    })
                }
            },
        )
    }
}

private const val TRACKPOINT_BATCH_SIZE = 50
internal const val MAX_SAVED_TRACKPOINTS = 1000

@Serializable
private data class SavedTrack(val points: List<Trackpoint>, val isRecording: Boolean)
