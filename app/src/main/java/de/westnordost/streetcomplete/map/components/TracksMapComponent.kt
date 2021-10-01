package de.westnordost.streetcomplete.map.components

import android.location.Location
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.geometry.Polyline
import de.westnordost.streetcomplete.map.tangram.KtMapController
import de.westnordost.streetcomplete.map.tangram.toLngLat
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
    private var tracks: MutableList<MutableList<LngLat>>

    init {
        tracks = ArrayList()
        tracks.add(ArrayList())
    }

    /** Add a point to the current track */
    fun addToCurrentTrack(pos: Location) {
        val track = tracks.last()
        track.add(pos.toLngLat())

        // every 100th trackpoint, move the index to the back
        if (track.size - index > 100) {
            putAllTracksInOldLayer()
        } else {
            layer1.setFeatures(listOf(track.subList(index, track.size).toPolyline(false)))
        }
    }

    /** Start a new track. I.e. the points in that track will be drawn as an own polyline */
    fun startNewTrack() {
        tracks.add(ArrayList())
        putAllTracksInOldLayer()
    }

    /** Set all the tracks (when re-initializing) */
    fun setTracks(tracks: List<List<Location>>) {
        this.tracks = tracks.map { track -> track.map { it.toLngLat() }.toMutableList() }.toMutableList()
        putAllTracksInOldLayer()
    }

    private fun putAllTracksInOldLayer() {
        index = max(0, tracks.last().lastIndex)
        layer1.clear()
        layer2.setFeatures(tracks.map { it.toPolyline(true) })
    }

    fun clear() {
        tracks = ArrayList()
        startNewTrack()
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the layer
        private const val LAYER1 = "streetcomplete_track"
        private const val LAYER2 = "streetcomplete_track2"

    }
}

private fun List<LngLat>.toPolyline(old: Boolean) =
    Polyline(this, mapOf("type" to "line", "old" to old.toString()))
