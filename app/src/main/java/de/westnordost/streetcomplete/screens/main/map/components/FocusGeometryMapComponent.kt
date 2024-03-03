package de.westnordost.streetcomplete.screens.main.map.components

import android.content.ContentResolver
import android.graphics.RectF
import androidx.annotation.UiThread
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.getEnclosingCamera
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreFeature
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Display element geometry and enables focussing on given geometry. I.e. to highlight the geometry
 *  of the element a selected quest refers to. Also zooms to the element in question so that it is
 *  contained in the screen area */
class FocusGeometryMapComponent(private val contentResolver: ContentResolver, private val map: MapboxMap) {

    private val focusedGeometrySource = GeoJsonSource(SOURCE)

    private var previousCameraPosition: CameraPosition? = null

    /** Returns whether beginFocusGeometry() was called earlier but not endFocusGeometry() yet */
    val isZoomedToContainGeometry: Boolean get() =
        previousCameraPosition != null

    val layers: List<Layer> = listOf(
        FillLayer("focus-geo-fill", SOURCE)
            .withFilter(eq(get("type"), literal("polygon")))
            .withProperties(
                fillColor("#D14000"),
                fillOpacity(0.3f)
            ),
        // TODO low prio: animation of width+alpha (breathing selection effect). From shader:
        //  opacity = min(max(sin(u_time * 3.0) / 2.0 + 0.5, 0.125), 0.875) * 0.5 + 0.125;
        //  width *= min(max(-sin(u_time * 3.0) / 2.0 + 0.5, 0.125), 0.875) + 0.625;
        LineLayer("focus-geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(10f),
                lineColor("#D14000"),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        CircleLayer("focus-geo-circle", SOURCE)
            .withProperties(
                circleColor("#D14000"),
                circleOpacity(0.7f)
            ),
    )

    init {
        map.style?.addSource(focusedGeometrySource)
    }

    /** Show the given geometry. Previously shown geometry is replaced. */
    @UiThread fun showGeometry(geometry: ElementGeometry) {
        focusedGeometrySource.setGeoJson(geometry.toMapLibreFeature())
    }

    /** Hide all shown geometry */
    @UiThread fun clearGeometry() {
        focusedGeometrySource.clear()
    }

    @UiThread fun beginFocusGeometry(g: ElementGeometry, offset: RectF) {
        val targetPos = map.getEnclosingCamera(g, offset) ?: return

        val currentPos = map.camera
        // limit max zoom to not zoom in to the max when zooming in on points;
        // also zoom in a bit less to have a padding around the zoomed-in element
        val targetZoom = min(targetPos.zoom - 0.5, 21.0)

        val zoomDiff = abs(currentPos.zoom - targetZoom)
        val zoomTime = max(450, (zoomDiff * 300).roundToInt())

        map.updateCamera(zoomTime, contentResolver) {
            position = targetPos.position
            zoom = targetZoom
            padding = targetPos.padding
        }

        if (previousCameraPosition == null) previousCameraPosition = currentPos
    }

    @UiThread fun clearFocusGeometry() {
        previousCameraPosition = null
    }

    @UiThread fun endFocusGeometry() {
        val pos = previousCameraPosition
        if (pos != null) {
            val currentPos = map.cameraPosition
            val zoomTime = max(300, (abs(currentPos.zoom - pos.zoom) * 300).roundToInt())

            map.updateCamera(zoomTime, contentResolver) {
                position = pos.position
                zoom = pos.zoom
            }
        }
        previousCameraPosition = null
    }

    companion object {
        private const val SOURCE = "focus-geometry-source"
    }
}
