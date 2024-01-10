package de.westnordost.streetcomplete.screens.main.map.components

import android.graphics.RectF
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** Display element geometry and enables focussing on given geometry. I.e. to highlight the geometry
 *  of the element a selected quest refers to. Also zooms to the element in question so that it is
 *  contained in the screen area */
class FocusGeometryMapComponent(private val ctrl: KtMapController, private val mapboxMap: MapboxMap) {

//    private val geometryLayer: MapData = ctrl.addDataLayer(GEOMETRY_LAYER)

    private var previousCameraPosition: CameraPosition? = null

    /** Returns whether beginFocusGeometry() was called earlier but not endFocusGeometry() yet */
    val isZoomedToContainGeometry: Boolean get() =
        previousCameraPosition != null

    /** Show the given geometry. Previously shown geometry is replaced. */
    fun showGeometry(geometry: ElementGeometry) {
        MainMapFragment.geometryLineManager?.deleteAll()
        MainMapFragment.geometryCircleManger?.deleteAll()
        MainMapFragment.geometryFillManager?.deleteAll()

        when (geometry) {
            is ElementPolylinesGeometry -> {
                val points = geometry.polylines.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points)
                MainMapFragment.geometrySource?.setGeoJson(Feature.fromGeometry(multilineString))
            }
            is ElementPolygonsGeometry -> {
                val points = geometry.polygons.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val polygon = com.mapbox.geojson.Polygon.fromLngLats(points) // todo: breaks for mulitpolygons when zooming in (weird...)
                // todo: actually the outline is displayed in the fill layer
                //  maybe this is what breaks multipolygon display
                //  just set some Expression.geometryType() filter on the fill layer
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points) // outline
                MainMapFragment.geometrySource?.setGeoJson(
                    FeatureCollection.fromFeatures(listOf(
                        Feature.fromGeometry(multilineString), Feature.fromGeometry(polygon))))
            }
            is ElementPointGeometry -> {
                MainMapFragment.geometrySource?.setGeoJson(com.mapbox.geojson.Point.fromLngLat(geometry.center.longitude, geometry.center.latitude))
            }
        }
    }

    /** Hide all shown geometry */
    fun clearGeometry() {
        MainMapFragment.geometryLineManager?.deleteAll()
        MainMapFragment.geometryCircleManger?.deleteAll()
        MainMapFragment.geometryFillManager?.deleteAll()
    }

    @Synchronized fun beginFocusGeometry(g: ElementGeometry, offset: RectF) {
        val pos = ctrl.getEnclosingCameraPosition(g.getBounds(), offset) ?: return
        val currentPos = ctrl.cameraPosition
        val targetZoom = min(pos.zoom.toFloat(), 20f)

        // do not zoom in if the element is already nicely in the view
        if (ctrl.screenAreaContains(g, RectF()) && targetZoom - currentPos.zoom < 2.5) return

        if (previousCameraPosition == null) previousCameraPosition = currentPos

        val zoomTime = max(450L, (abs(currentPos.zoom - targetZoom) * 300).roundToLong())

        // test implementation for showing that mapLibre can set camera to the desired area, including padding
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
            c?.let { MainMapFragment.mapboxMap!!.easeCamera(CameraUpdateFactory.newCameraPosition(it), zoomTime.toInt()) }
        }

        // commented because otherwise camera will be moved by cameraManager too
        // this means that when clicking on an element / quest, only the mapLibre camera may move
        //  (and only if the geometry is not fully inside tangram camera view...)
/*        ctrl.updateCameraPosition(zoomTime, DecelerateInterpolator()) {
            position = pos.position
            zoom = targetZoom
        }
*/    }

    @Synchronized fun clearFocusGeometry() {
        previousCameraPosition = null
    }

    @Synchronized fun endFocusGeometry() {
        val pos = previousCameraPosition
        if (pos != null) {
            val currentPos = ctrl.cameraPosition
            val zoomTime = max(300L, (abs(currentPos.zoom - pos.zoom) * 300).roundToLong())

            ctrl.updateCameraPosition(zoomTime) {
                position = pos.position
                zoom = pos.zoom
                tilt = pos.tilt
                rotation = pos.rotation
            }
        }
        previousCameraPosition = null
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val GEOMETRY_LAYER = "streetcomplete_geometry"
    }
}
