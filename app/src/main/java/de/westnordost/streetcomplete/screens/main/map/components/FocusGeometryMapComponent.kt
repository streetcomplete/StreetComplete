package de.westnordost.streetcomplete.screens.main.map.components

import android.graphics.RectF
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.ScCameraPosition
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toLatLon
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Display element geometry and enables focussing on given geometry. I.e. to highlight the geometry
 *  of the element a selected quest refers to. Also zooms to the element in question so that it is
 *  contained in the screen area */
class FocusGeometryMapComponent(private val ctrl: KtMapController, private val mapboxMap: MapboxMap) {

    private var previousCameraPosition: ScCameraPosition? = null

    /** Returns whether beginFocusGeometry() was called earlier but not endFocusGeometry() yet */
    val isZoomedToContainGeometry: Boolean get() =
        previousCameraPosition != null

    /** Show the given geometry. Previously shown geometry is replaced. */
    fun showGeometry(geometry: ElementGeometry) {

        when (geometry) {
            is ElementPolylinesGeometry -> {
                val points = geometry.polylines.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points)
                MainMapFragment.focusedGeometrySource?.setGeoJson(Feature.fromGeometry(multilineString).apply { addStringProperty("way", "yes") })
            }
            is ElementPolygonsGeometry -> {
                val points = geometry.polygons.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val polygon = com.mapbox.geojson.Polygon.fromLngLats(points)
                // todo: breaks for mulitpolygons at high zoom only
                // todo: actually the outline is displayed in the fill layer
                //  maybe this is what breaks multipolygon display
                //  just set some Expression.geometryType() filter on the fill layer
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points) // outline
                MainMapFragment.focusedGeometrySource?.setGeoJson(
                    FeatureCollection.fromFeatures(listOf(
                        Feature.fromGeometry(multilineString), Feature.fromGeometry(polygon))))
            }
            is ElementPointGeometry -> {
                MainMapFragment.focusedGeometrySource?.setGeoJson(com.mapbox.geojson.Point.fromLngLat(geometry.center.longitude, geometry.center.latitude))
            }
        }
    }

    /** Hide all shown geometry */
    fun clearGeometry() {
        val fc: FeatureCollection? = null
        MainMapFragment.focusedGeometrySource?.setGeoJson(fc)
    }

    @Synchronized fun beginFocusGeometry(g: ElementGeometry, offset: RectF) {
        val pos = ctrl.getEnclosingCameraPosition(g.getBounds(), offset) ?: return
        val currentPos = ctrl.cameraPosition
        val targetZoom = min(pos.zoom.toFloat(), 20f)

        // do not zoom in if the element is already nicely in the view
        if (ctrl.screenAreaContains(g, RectF()) && targetZoom - currentPos.zoom < 2.5) return

        if (previousCameraPosition == null) previousCameraPosition = currentPos

        val zoomTime = max(450, (abs(currentPos.zoom - targetZoom) * 300).roundToInt())

        // todo: works, but seems needlessly complicated
        // and still might have some issues
        MainActivity.activity!!.runOnUiThread {
            val bounds = LatLngBounds.fromLatLngs(listOf(g.getBounds().max.toLatLng(), g.getBounds().min.toLatLng()))
            val c = MainMapFragment.mapboxMap!!.getCameraForLatLngBounds(
                bounds,
                arrayOf(
                    offset.left.toInt(),
                    offset.top.toInt(),
                    offset.right.toInt(),
                    offset.bottom.toInt()
                ).toIntArray()
            )
            c?.let {
                if (g is ElementPointGeometry) {
                    // because of target zoom
                    val update = CameraUpdate().apply {
                        zoom = targetZoom.toDouble()
                        position = it.target?.toLatLon()
                    }
                    ctrl.updateCameraPosition(zoomTime, update)
                } else {
                    // above is nice for nodes, but actually it gets the wrong position (ignores padding)
                    MainMapFragment.mapboxMap!!.easeCamera(CameraUpdateFactory.newCameraPosition(it), zoomTime.toInt())
                }
            }
        }
    }

    @Synchronized fun clearFocusGeometry() {
        previousCameraPosition = null
    }

    @Synchronized fun endFocusGeometry() {
        val pos = previousCameraPosition
        if (pos != null) {
            val currentPos = ctrl.cameraPosition
            val zoomTime = max(300, (abs(currentPos.zoom - pos.zoom) * 300).roundToInt())

            ctrl.updateCameraPosition(zoomTime) {
                position = pos.position
                zoom = pos.zoom
                tilt = pos.tilt
                rotation = pos.rotation
            }
        }
        previousCameraPosition = null
    }
}
