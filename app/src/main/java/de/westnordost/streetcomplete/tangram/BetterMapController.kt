package de.westnordost.streetcomplete.tangram

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.TypeConverter
import android.animation.TypeEvaluator
import android.graphics.PointF
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.mapzen.tangram.*

class BetterMapController(private val c: MapController) {

    // TODO could use suspend
    fun captureFrame(callback: MapController.FrameCaptureCallback, waitForCompleteView: Boolean) =
            c.captureFrame(callback, waitForCompleteView)

    val glViewHolder = c.glViewHolder

    fun loadSceneFile(
            path: String,
            sceneUpdates: List<SceneUpdate>? = null
    ) = c.loadSceneFile(path, sceneUpdates)
    fun loadSceneFileAsync(
            path: String,
            sceneUpdates: List<SceneUpdate>? = null
    ) = c.loadSceneFileAsync(path, sceneUpdates)

    fun loadSceneYaml(
            yaml: String,
            resourceRoot: String,
            sceneUpdates: List<SceneUpdate>? = null
    ) = c.loadSceneYaml(yaml, resourceRoot, sceneUpdates)

    // TODO could use suspend
    fun loadSceneYamlAsync(
            yaml: String,
            resourceRoot: String,
            sceneUpdates: List<SceneUpdate>? = null
    ) = c.loadSceneYamlAsync(yaml, resourceRoot, sceneUpdates)

    // TODO probably remove/replace
    fun flyToCameraPosition(
            position: CameraPosition,
            callback: MapController.CameraAnimationCallback? = null,
            duration: Int = 0,
            speed: Float = 1f
    ) = c.flyToCameraPosition(position, callback, speed)
    fun cancelCameraAnimation() = c.cancelCameraAnimation()

    var cameraType
        set(value) { c.cameraType = value }
        get() = c.cameraType

    var minimumZoomLevel
        set(value) { c.minimumZoomLevel = value }
        get() = c.minimumZoomLevel

    var maximumZoomLevel
        set(value) { c.maximumZoomLevel = value }
        get() = c.maximumZoomLevel

    fun screenPositionToLngLat(screenPosition: PointF) = c.screenPositionToLngLat(screenPosition)
    fun lngLatToScreenPosition(lngLat: LngLat) = c.lngLatToScreenPosition(lngLat)

    fun addDataLayer(name: String, generateCentroid: Boolean = false) = c.addDataLayer(name, generateCentroid)

    fun requestRender() = c.requestRender()
    fun setRenderMode(renderMode: Int) = c.setRenderMode(renderMode)

    val touchInput get() = c.touchInput

    fun setPickRadius(radius: Float) = c.setPickRadius(radius)

    fun setFeaturePickListener(listener: FeaturePickListener?) = c.setFeaturePickListener(listener)
    fun setSceneLoadListener(listener: MapController.SceneLoadListener?) = c.setSceneLoadListener(listener)
    fun setLabelPickListener(listener: LabelPickListener?) = c.setLabelPickListener(listener)
    fun setMarkerPickListener(listener: MarkerPickListener?) = c.setMarkerPickListener(listener)

    // TODO this could be suspend...
    fun pickLabel(posX: Float, posY: Float) : = c.pickLabel(posX, posY)
    // TODO this could be suspend...
    fun pickMarker(posX: Float, posY: Float) = c.pickMarker(posX, posY)

    fun addMarker() = c.addMarker()
    fun removeMarker(marker: Marker) = c.removeMarker(marker)
    fun removeMarker(markerId: Long) = c.removeMarker(markerId)
    fun removeAllMarkers() = c.removeAllMarkers()

    fun setMapChangeListener(listener: MapChangeListener?) = c.setMapChangeListener(listener)

    fun queueEvent(r: Runnable) = c.queueEvent(r)

    fun setDebugFlag(flag: MapController.DebugFlag, on: Boolean) = c.setDebugFlag(flag, on)

    fun useCachedGlState(use: Boolean) = c.useCachedGlState(use)

    fun setDefaultBackgroundColor(red: Float, green: Float, blue: Float) = c.setDefaultBackgroundColor(red, green, blue)
}

// TODO flyTo?

fun MapController.getDisplayedArea(padding: RectF): LngLatBounds? {
    val view = glViewHolder?.view ?: return null
    if (view.width == 0) return null
    if (view.height == 0) return null

    val size = PointF(
            view.width - padding.left - padding.right,
            view.height - padding.top - padding.bottom)

    // the special cases here are: map tilt and map rotation:
    // * map tilt makes the screen area -> world map area into a trapezoid
    // * map rotation makes the screen area -> world map area into a rotated rectangle
    // dealing with tilt: this method is just not defined if the tilt is above a certain limit
    if (tilt > Math.PI / 4f) return null // 45°

    val positions = arrayOf(
            screenPositionToLngLat(PointF(padding.left, padding.top)),
            screenPositionToLngLat(PointF(padding.left + size.x, padding.top)),
            screenPositionToLngLat(PointF(padding.left, padding.top + size.y)),
            screenPositionToLngLat(PointF(padding.left + size.x, padding.top + size.y))
    ).filterNotNull()
    if (positions.isEmpty()) return null

    // dealing with rotation: find each the largest latlon and the smallest latlon, that'll
    // be our bounding box
    val latMin = positions.minBy { it.latitude }!!.latitude
    val lonMin = positions.minBy { it.longitude }!!.longitude
    val latMax = positions.maxBy { it.latitude }!!.latitude
    val lonMax = positions.maxBy { it.longitude}!!.longitude

    return LngLatBounds(LngLat(lonMin, latMin), LngLat(lonMax, latMax))
}

data class LngLatBounds(val sw: LngLat, val ne: LngLat)
