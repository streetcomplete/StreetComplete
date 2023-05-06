package de.westnordost.streetcomplete.screens.main.map.components

import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.mapzen.tangram.MapData
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.screenAreaContains
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** Display element geometry and enables focussing on given geometry. I.e. to highlight the geometry
 *  of the element a selected quest refers to. Also zooms to the element in question so that it is
 *  contained in the screen area */
class FocusGeometryMapComponent(private val ctrl: KtMapController) {

    private val geometryLayer: MapData = ctrl.addDataLayer(GEOMETRY_LAYER)

    private var previousCameraPosition: CameraPosition? = null

    /** Returns whether beginFocusGeometry() was called earlier but not endFocusGeometry() yet */
    val isZoomedToContainGeometry: Boolean get() =
        previousCameraPosition != null

    /** Show the given geometry. Previously shown geometry is replaced. */
    fun showGeometry(geometry: ElementGeometry) {
        geometryLayer.setFeatures(geometry.toTangramGeometry())
    }

    /** Hide all shown geometry */
    fun clearGeometry() {
        geometryLayer.clear()
    }

    @Synchronized fun beginFocusGeometry(g: ElementGeometry, offset: RectF) {
        val pos = ctrl.getEnclosingCameraPosition(g.getBounds(), offset) ?: return
        val currentPos = ctrl.cameraPosition
        val targetZoom = min(pos.zoom, 20f)

        // do not zoom in if the element is already nicely in the view
        if (ctrl.screenAreaContains(g, RectF()) && targetZoom - currentPos.zoom < 2.5) return

        if (previousCameraPosition == null) previousCameraPosition = currentPos

        val zoomTime = max(450L, (abs(currentPos.zoom - targetZoom) * 300).roundToLong())

        ctrl.updateCameraPosition(zoomTime, DecelerateInterpolator()) {
            position = pos.position
            zoom = targetZoom
        }
    }

    @Synchronized fun clearFocusGeometry() {
        previousCameraPosition = null
    }

    @Synchronized fun endFocusGeometry() {
        val pos = previousCameraPosition
        if (pos != null) {
            val currentPos = ctrl.cameraPosition
            val zoomTime = max(300L, (abs(currentPos.zoom - pos.zoom) * 300).roundToLong())

            ctrl.updateCameraPosition(zoomTime, AccelerateDecelerateInterpolator()) {
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
