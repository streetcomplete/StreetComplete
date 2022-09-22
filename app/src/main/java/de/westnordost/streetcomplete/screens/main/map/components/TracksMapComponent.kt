package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.LngLat
import com.mapzen.tangram.geometry.Polyline
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLngLat
import kotlin.math.max

/** Takes care of showing the path(s) walked on the map */
class TracksMapComponent(ctrl: KtMapController) {

    /* There are two layers simply as a performance optimization: If there are thousands of
       trackpoints, we don't want to update (=copy) the thousands of points each time a new
       trackpoint is added. Instead, we only update a list of 100 trackpoints each time a new
       trackpoint is added and every 100th time, we update the other layer.

       So, the list of points updated ~per second doesn't grow too long.
     */
    private val layer1 = ctrl.addDataLayer(LAYER1)
    private val layer2 = ctrl.addDataLayer(LAYER2)

    private var index = 0
    private data class Track(val trackpoints: MutableList<LngLat>, val isRecording: Boolean)
    private var tracks: MutableList<Track> = arrayListOf(Track(ArrayList(), false))

    /** Add a point to the current track */
    fun addToCurrentTrack(pos: LatLon) {
        val track = tracks.last()
        track.trackpoints.add(pos.toLngLat())
        val trackpoints = track.trackpoints

        // every 100th trackpoint, move the index to the back
        if (trackpoints.size - index > 100) {
            putAllTracksInOldLayer()
        } else {
            layer1.setFeatures(listOf(trackpoints.subList(index, trackpoints.size).toPolyline(false, track.isRecording)))
        }
    }

    /** Start a new track. I.e. the points in that track will be drawn as an own polyline */
    fun startNewTrack(record: Boolean) {
        tracks.add(Track(ArrayList(), record))
        putAllTracksInOldLayer()
    }

    /** Set all the tracks (when re-initializing), if recording the last track is the only recording */
    fun setTracks(tracks: List<List<LatLon>>, isRecording: Boolean) {
        this.tracks = tracks.mapIndexed { index, track ->
            var recording = false
            if (isRecording && index == tracks.size - 1) {
                recording = true
            }
            Track(track.map { it.toLngLat() }.toMutableList(), recording)
        }.toMutableList()
        putAllTracksInOldLayer()
    }

    private fun putAllTracksInOldLayer() {
        index = max(0, tracks.last().trackpoints.lastIndex)
        layer1.clear()
        layer2.setFeatures(tracks.map { it.trackpoints.toPolyline(true, it.isRecording) })
    }

    fun clear() {
        tracks = ArrayList()
        startNewTrack(false)
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the layer
        private const val LAYER1 = "streetcomplete_track"
        private const val LAYER2 = "streetcomplete_track2"
    }
}

private fun List<LngLat>.toPolyline(old: Boolean, record: Boolean) =
    Polyline(this, listOfNotNull(
        "type" to "line",
        "old" to old.toString(),
        if (record) ("record" to "true") else null
    ).toMap())
