package de.westnordost.streetcomplete.screens.main.map.components

import android.animation.TimeAnimator
import android.content.ContentResolver
import android.provider.Settings
import androidx.annotation.UiThread
import androidx.core.graphics.Insets
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.Padding
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.getEnclosingCamera
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.roundToInt

/** Display element geometry and enables focussing on given geometry. I.e. to highlight the geometry
 *  of the element a selected quest refers to. Also zooms to the element in question so that it is
 *  contained in the screen area */
class FocusGeometryMapComponent(private val contentResolver: ContentResolver, private val map: MapLibreMap)
    : DefaultLifecycleObserver {

    private val focusedGeometrySource = GeoJsonSource(SOURCE)

    private var previousCameraPosition: CameraPosition? = null

    private val animation: TimeAnimator
    private var animationTick: Int = 0

    val layers: List<Layer> = listOf(
        FillLayer("focus-geo-fill", SOURCE)
            .withFilter(isArea())
            .withProperties(
                fillColor("#D14000"),
                fillOpacity(0.3f)
            ),
        LineLayer("focus-geo-lines", SOURCE)
            // both polygon and line
            .withProperties(
                lineWidth(10f),
                lineColor("#D14000"),
                lineOpacity(0.7f),
                lineCap(Property.LINE_CAP_ROUND)
            ),
        CircleLayer("focus-geo-circle", SOURCE)
            .withFilter(isPoint())
            .withProperties(
                circleColor("#D14000"),
                circleRadius(12f),
                circleOpacity(0.7f)
            ),
    )

    init {
        focusedGeometrySource.isVolatile = true
        map.style?.addSource(focusedGeometrySource)
        animation = TimeAnimator()
        animation.setTimeListener { _, _, _ ->
            // we don't care about delta time etc because if this function is called rarely
            // when device slow etc, the animation should just slow down rather than look
            // jarring
            animateGeometry()
        }
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

    /** Show the given geometry. Previously shown geometry is replaced. */
    @UiThread fun showGeometry(geometry: ElementGeometry) {
        focusedGeometrySource.setGeoJson(geometry.toMapLibreGeometry())
        val animatorDurationScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (animatorDurationScale > 0f) animation.start()
    }

    private fun animateGeometry() {
        // rather than editing the style, it is recommended to use feature-state for things like
        // this. However, this is not implemented on Android yet. See
        // https://github.com/maplibre/maplibre-native/issues/1698
        animationTick++
        val breathing = (sin(animationTick++ * 0.03f) / 2f + 0.5f) // 0.0 .. 1.0
        val widthFactor = breathing + 0.75f // 0.75 .. 1.5
        val opacity = (1f - breathing) * 0.5f + 0.15f // 0.525 .. 0.9
        map.style?.getLayerAs<LineLayer>("focus-geo-lines")?.setProperties(
            lineWidth(10f * widthFactor),
            lineOpacity(opacity),
        )
        map.style?.getLayerAs<CircleLayer>("focus-geo-circle")?.setProperties(
            circleRadius(12f * widthFactor),
            circleOpacity(opacity)
        )
    }

    /** Hide all shown geometry */
    @UiThread fun clearGeometry() {
        focusedGeometrySource.clear()
        animation.end()
    }

    @UiThread fun beginFocusGeometry(g: ElementGeometry, insets: Insets) {
        val targetPos = map.getEnclosingCamera(g, insets) ?: return

        val currentPos = map.camera
        // limit max zoom to not zoom in to the max when zooming in on points;
        // also zoom in a bit less to have a padding around the zoomed-in element
        val targetZoom = min(targetPos.zoom - 0.75, 19.0)

        val zoomDiff = abs(currentPos.zoom - targetZoom)
        val zoomTime = max(450, (zoomDiff * 450).roundToInt())

        map.updateCamera(zoomTime, contentResolver) {
            position = targetPos.position
            padding = targetPos.padding
            // also, only zoom if diff big enough
            if (zoomDiff > 0.5) zoom = targetZoom
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
                padding = Padding(0.0, 0.0, 0.0, 0.0)
            }
        }
        previousCameraPosition = null
    }

    companion object {
        private const val SOURCE = "focus-geometry-source"
    }
}
