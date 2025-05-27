package de.westnordost.streetcomplete.screens.main.map.components

import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.UiThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.JsonObject
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.math.max

/** Takes care of showing the path(s) walked on the map */
class TracksMapComponent(context: Context, mapStyle: Style, private val map: MapLibreMap) :
    DefaultLifecycleObserver {
    /* There are two sources simply as a performance optimization: If there are thousands of
       trackpoints, we don't want to update (=copy) the thousands of points each time a new
       trackpoint is added. Instead, we only update a list of 100 trackpoints each time a new
       trackpoint is added and every 100th time, we update the other layer.

       So, the list of points updated ~per second doesn't grow too long.
       The animation source is just the last line, whose progress is animated.
     */
    private val trackAnimationSource = GeoJsonSource("animate-track-source")
    private val animation = ValueAnimator.ofFloat(0f, 1f)

    private val trackSource = GeoJsonSource("track-source")
    private val oldTrackSource = GeoJsonSource("old-track-source")

    private data class Track(val trackpoints: MutableList<LatLon>, val isRecording: Boolean)
    private var track: Track = Track(ArrayList(), false)
    private var oldTracks: MutableList<MutableList<LatLon>> = arrayListOf()

    private val commonTrackProperties get() = if (!isApril1st()) arrayOf(
        lineWidth(6f),
        lineColor(switchCase(
            get("recording"), literal("#fe1616"),
            literal("#536dfe")
        ))
    ) else arrayOf(
        lineWidth(26f),
        linePattern(switchCase(
            get("recording"), literal("trackRecordImg"),
            literal("trackImg")
        ))
    )

    val layers: List<Layer> = listOf(
        LineLayer("animate-track", "animate-track-source")
            .withProperties(*commonTrackProperties, lineOpacity(0.6f), lineCap(Property.LINE_CAP_ROUND), lineDasharray(arrayOf(0f, 2f))),
        LineLayer("track", "track-source")
            .withProperties(*commonTrackProperties, lineOpacity(0.6f), lineCap(Property.LINE_CAP_ROUND), lineDasharray(arrayOf(0f, 2f))),
        LineLayer("old-track", "old-track-source")
            .withProperties(*commonTrackProperties, lineOpacity(0.2f), lineCap(Property.LINE_CAP_ROUND), lineDasharray(arrayOf(0f, 2f)))
    )

    init {
        trackAnimationSource.isVolatile = true
        trackSource.isVolatile = true
        oldTrackSource.isVolatile = true

        animation.duration = 600L
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.addUpdateListener { updateAnimatedTrack(it.animatedValue as Float) }

        if (isApril1st()) {
            mapStyle.addImage("trackImg", context.getDrawable(R.drawable.track_nyan)!!)
            mapStyle.addImage("trackRecordImg", context.getDrawable(R.drawable.track_nyan_record)!!)
        }

        map.style?.addSource(trackAnimationSource)
        map.style?.addSource(trackSource)
        map.style?.addSource(oldTrackSource)
    }

    override fun onPause(owner: LifecycleOwner) {
        animation.pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        animation.resume()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        animation.cancel()
    }

    /** Add a point to the current track */
    @UiThread fun addToCurrentTrack(pos: LatLon) {
        track.trackpoints.add(pos)
        // every 100th trackpoint, move older trackpoints to old tracks
        if (track.trackpoints.size > 100) {
            oldTracks.add(track.trackpoints.subList(0, 50).toMutableList())
            val lastPoints = track.trackpoints.subList(50, track.trackpoints.size).toMutableList()
            track.trackpoints.clear()
            track.trackpoints.addAll(lastPoints)
            updateOldTracks()
        }
        updateTrack()
    }

    /** Start a new track. I.e. the points in that track will be drawn as an own polyline */
    @UiThread fun startNewTrack(record: Boolean) {
        animation.cancel()
        oldTracks.add(track.trackpoints)
        track = Track(ArrayList(), record)
        updateOldTracks()
        trackSource.clear()
        trackAnimationSource.clear()
    }

    /** Set all the tracks (when re-initializing) */
    @UiThread fun setTracks(pointsList: List<List<LatLon>>, isRecording: Boolean) {
        require(pointsList.isNotEmpty())
        oldTracks = pointsList.map { it.toMutableList() }.toMutableList()
        track = Track(ArrayList(), isRecording)
        updateOldTracks()
    }

    @UiThread fun clear() {
        animation.cancel()
        oldTracks.clear()
        track = Track(ArrayList(), false)
        trackSource.clear()
        oldTrackSource.clear()
        trackAnimationSource.clear()
    }

    private fun updateAnimatedTrack(progress: Float) {
        val size = track.trackpoints.size
        if (size < 2) return
        val s = track.trackpoints[size - 2]
        val e = track.trackpoints[size - 1]
        val animated = LatLon(
            latitude = s.latitude + (e.latitude - s.latitude) * progress,
            longitude = normalizeLongitude(s.longitude + (e.longitude - s.longitude) * progress)
        )
        trackAnimationSource.setGeoJson(listOf(s, animated).toLineFeature(track.isRecording))
    }

    private fun updateTrack() {
        val trackpoints = track.trackpoints
        val pointsExceptLast = trackpoints.subList(0, max(0, trackpoints.size - 1))
        trackSource.setGeoJson(pointsExceptLast.toLineFeature(track.isRecording))
        if (trackpoints.size >= 2) {
            animation.start()
        }
    }

    private fun updateOldTracks() {
        val features = oldTracks.map { it.toLineFeature(false) }
        oldTrackSource.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}

private fun List<LatLon>.toLineFeature(record: Boolean): Feature {
    val line = LineString.fromLngLats(map { Point.fromLngLat(it.longitude, it.latitude) })
    val p = JsonObject()
    p.addProperty("recording", record)
    return Feature.fromGeometry(line, p)
}
