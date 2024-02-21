package de.westnordost.streetcomplete.screens.main.map.components

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.clear
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import kotlin.math.max

/** Takes care of showing the path(s) walked on the map */
class TracksMapComponent(ctrl: KtMapController) {
    /* There are two layers simply as a performance optimization: If there are thousands of
       trackpoints, we don't want to update (=copy) the thousands of points each time a new
       trackpoint is added. Instead, we only update a list of 100 trackpoints each time a new
       trackpoint is added and every 100th time, we update the other layer.

       So, the list of points updated ~per second doesn't grow too long.
     */

    private var index = 0
    private data class Track(val trackpoints: MutableList<LatLng>, val isRecording: Boolean)
    private var tracks: MutableList<Track> = arrayListOf(Track(ArrayList(), false))

    /** Add a point to the current track */
    fun addToCurrentTrack(pos: LatLon) {
        val track = tracks.last()
        track.trackpoints.add(pos.toLatLng())
        val trackpoints = track.trackpoints

        // every 100th trackpoint, move the index to the back
        if (trackpoints.size - index > 100) {
            putAllTracksInOldLayer()
        } else {
            MainMapFragment.trackSource?.setGeoJson(trackpoints.toLineFeature(track.isRecording))
        }
    }

    /** Start a new track. I.e. the points in that track will be drawn as an own polyline */
    fun startNewTrack(record: Boolean) {
        tracks.add(Track(ArrayList(), record))
        putAllTracksInOldLayer()
    }

    /** Set all the tracks (when re-initializing), if recording the last track is the only recording */
    fun setTracks(pointsList: List<List<LatLon>>, isRecording: Boolean) {
        require(pointsList.isNotEmpty())
        tracks = pointsList.mapIndexed { index, track ->
            var recording = false
            if (isRecording && index == pointsList.size - 1) {
                recording = true
            }
            Track(track.map { it.toLatLng() }.toMutableList(), recording)
        }.toMutableList()
        putAllTracksInOldLayer()
    }

    private fun putAllTracksInOldLayer() {
        index = max(0, tracks.last().trackpoints.lastIndex)
        MainMapFragment.trackSource?.clear()
        val features = tracks.map { it.trackpoints.toLineFeature(it.isRecording) }
        MainMapFragment.oldTrackSource?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    fun clear() {
        tracks = ArrayList()
        startNewTrack(false)
    }
}

private fun List<LatLng>.toLineFeature(record: Boolean): Feature {
    val line = LineString.fromLngLats(map { Point.fromLngLat(it.longitude, it.latitude) })
    val p = JsonObject()
    p.addProperty("recording", record) // todo: this is not used (and possibly it's easier to only set it if true)
    return Feature.fromGeometry(line, p)
}
