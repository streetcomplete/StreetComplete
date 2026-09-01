package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.units.International

/** Owns GPS track accumulation independently of any platform map view or lifecycle bundle. */
@Stable
internal class MainMapTrackState(
    snapshot: MainMapTrackSnapshot = MainMapTrackSnapshot(),
) {
    var displayedMeasurement by mutableStateOf(snapshot.displayedMeasurement)
        private set
    var isRecording by mutableStateOf(snapshot.isRecording)
        private set
    var currentRenderedTrack by mutableStateOf<List<LatLon>>(emptyList())
        private set
    var oldRenderedTracks by mutableStateOf<List<List<LatLon>>>(emptyList())
        private set

    private var tracks = snapshot.tracks
        .map { it.toMutableList() }
        .toMutableList()
        .also { if (it.isEmpty()) it.add(mutableListOf()) }

    init {
        rebuildRenderedTracks()
    }

    val currentTrack: List<Trackpoint> get() = tracks.last()

    fun onLocationMeasurement(measurement: LocationMeasurement) {
        displayedMeasurement = measurement
        val accuracy = measurement.horizontalAccuracy?.toFloat(International.Meters)
        if (accuracy != null && accuracy > MIN_TRACK_ACCURACY) return

        val last = tracks.last().lastOrNull()
        if (last != null && !isRecording &&
            measurement.measuredAt.toEpochMilliseconds() - last.time > MAX_TIME_BETWEEN_LOCATIONS
        ) {
            startNewTrack()
        }

        val trackpoint = Trackpoint(
            position = LatLon(measurement.position.latitude, measurement.position.longitude),
            time = measurement.measuredAt.toEpochMilliseconds(),
            accuracy = accuracy ?: 0f,
            elevation = measurement.position.altitude?.toFloat() ?: 0f,
        )
        tracks.last() += trackpoint
        currentRenderedTrack = currentRenderedTrack + trackpoint.position
        if (currentRenderedTrack.size > MAX_CURRENT_RENDERED_TRACK_POINTS) {
            oldRenderedTracks = oldRenderedTracks +
                listOf(currentRenderedTrack.take(RENDERED_TRACK_CHUNK_SIZE))
            currentRenderedTrack = currentRenderedTrack.drop(RENDERED_TRACK_CHUNK_SIZE)
        }
    }

    fun onLocationUnavailable() {
        displayedMeasurement = null
        tracks = mutableListOf(mutableListOf())
        currentRenderedTrack = emptyList()
        oldRenderedTracks = emptyList()
        // Keep recording active so capture resumes when the provider recovers. The Android
        // component cleared its red style here despite retaining the same logical flag.
    }

    fun startRecording() {
        if (isRecording) return
        isRecording = true
        startNewTrack()
    }

    fun stopRecording(): List<Trackpoint> {
        if (!isRecording) return emptyList()
        isRecording = false
        val recorded = tracks.last().toList()
        startNewTrack()
        return recorded
    }

    fun snapshot(): MainMapTrackSnapshot = MainMapTrackSnapshot(
        displayedMeasurement = displayedMeasurement,
        tracks = tracks.takeLastNested(MAX_SAVED_TRACK_POINTS),
        isRecording = isRecording,
    )

    private fun startNewTrack() {
        oldRenderedTracks = oldRenderedTracks + listOf(currentRenderedTrack)
        currentRenderedTrack = emptyList()
        tracks.add(mutableListOf())
    }

    private fun rebuildRenderedTracks() {
        val positions = tracks.map { track -> track.map(Trackpoint::position) }
        val old = positions.dropLast(1).toMutableList()
        var current = positions.lastOrNull().orEmpty()
        while (current.size > MAX_CURRENT_RENDERED_TRACK_POINTS) {
            old += current.take(RENDERED_TRACK_CHUNK_SIZE)
            current = current.drop(RENDERED_TRACK_CHUNK_SIZE)
        }
        oldRenderedTracks = old
        currentRenderedTrack = current
    }

    companion object {
        val Saver = Saver<MainMapTrackState, String>(
            save = { Json.encodeToString(it.snapshot()) },
            restore = { MainMapTrackState(Json.decodeFromString(it)) },
        )
    }
}

@Serializable
internal data class MainMapTrackSnapshot(
    val displayedMeasurement: LocationMeasurement? = null,
    val tracks: List<List<Trackpoint>> = listOf(emptyList()),
    val isRecording: Boolean = false,
)

private fun <T> List<List<T>>.takeLastNested(maxSize: Int): List<List<T>> {
    var remaining = maxSize
    val result = ArrayList<List<T>>()
    for (index in lastIndex downTo 0) {
        if (remaining <= 0) break
        val tail = this[index].takeLast(remaining)
        result.add(0, tail)
        remaining -= tail.size
    }
    return result.ifEmpty { listOf(emptyList()) }
}

private const val MIN_TRACK_ACCURACY = 20f
private const val MAX_TIME_BETWEEN_LOCATIONS = 60L * 1000
private const val MAX_CURRENT_RENDERED_TRACK_POINTS = 100
private const val RENDERED_TRACK_CHUNK_SIZE = 50
private const val MAX_SAVED_TRACK_POINTS = 1000
