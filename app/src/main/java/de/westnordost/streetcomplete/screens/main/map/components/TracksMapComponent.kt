package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import kotlin.math.max

/** Takes care of showing the path(s) walked on the map */
class TracksMapComponent(private val map: MapboxMap) {
    /* There are two sources simply as a performance optimization: If there are thousands of
       trackpoints, we don't want to update (=copy) the thousands of points each time a new
       trackpoint is added. Instead, we only update a list of 100 trackpoints each time a new
       trackpoint is added and every 100th time, we update the other layer.

       So, the list of points updated ~per second doesn't grow too long.
     */
    private val trackSource = GeoJsonSource("track-source")
    private val oldTrackSource = GeoJsonSource("old-track-source")

    private var index = 0
    private data class Track(val trackpoints: MutableList<LatLng>, val isRecording: Boolean)
    private var tracks: MutableList<Track> = arrayListOf(Track(ArrayList(), false))

    val layers: List<Layer> = listOf(
        LineLayer("track", "track-source")
            .withProperties(
                lineWidth(10f),
                lineColor(match(get("recording"),
                    literal(true), literal("#fe1616"),
                    literal("#536dfe")
                )),
                lineOpacity(0.3f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        LineLayer("old-track", "old-track-source")
            .withProperties(
                lineWidth(10f),
                lineColor(match(get("recording"),
                    literal(true), literal("#fe1616"),
                    literal("#536dfe")
                )),
                lineOpacity(0.15f),
                lineCap(Property.LINE_CAP_ROUND)
            )
    )

    init {
        map.style?.addSource(trackSource)
        map.style?.addSource(oldTrackSource)
    }

    /** Add a point to the current track */
    @UiThread fun addToCurrentTrack(pos: LatLon) {
        val track = tracks.last()
        track.trackpoints.add(pos.toLatLng())
        val trackpoints = track.trackpoints

        // every 100th trackpoint, move the index to the back
        if (trackpoints.size - index > 100) {
            putAllTracksInOldLayer()
        } else {
            trackSource.setGeoJson(trackpoints.toLineFeature(track.isRecording))
        }
    }

    /** Start a new track. I.e. the points in that track will be drawn as an own polyline */
    @UiThread fun startNewTrack(record: Boolean) {
        tracks.add(Track(ArrayList(), record))
        putAllTracksInOldLayer()
    }

    /** Set all the tracks (when re-initializing), if recording the last track is the only recording */
    @UiThread fun setTracks(pointsList: List<List<LatLon>>, isRecording: Boolean) {
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
        trackSource.clear()
        val features = tracks.map { it.trackpoints.toLineFeature(it.isRecording) }
        oldTrackSource.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    @UiThread fun clear() {
        tracks = ArrayList()
        startNewTrack(false)
    }
}

private fun List<LatLng>.toLineFeature(record: Boolean): Feature {
    val line = LineString.fromLngLats(map { Point.fromLngLat(it.longitude, it.latitude) })
    val p = JsonObject()
    p.addProperty("recording", record)
    return Feature.fromGeometry(line, p)
}
