package de.westnordost.streetcomplete.map.tangram

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.mapzen.tangram.*
import com.mapzen.tangram.networking.HttpHandler
import com.mapzen.tangram.viewholder.GLSurfaceViewHolderFactory
import com.mapzen.tangram.viewholder.GLViewHolder
import com.mapzen.tangram.viewholder.GLViewHolderFactory
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.util.SphericalEarthMath.enclosingBoundingBox
import de.westnordost.streetcomplete.util.SphericalEarthMath.normalizeLongitude
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/** Wrapper around the Tangram MapController. Features over the Tangram MapController (0.11.2):
 *  <br><br>
 *  <ul>
 *      <li>Update current scene with a list of scene updates (like in 0.9.6, see https://github.com/tangrams/tangram-es/issues/2107)</li>
 *      <li>Markers survive a scene updates</li>
 *      <li>Simultaneous camera animations are possible with a short and easy interface</li>
 *      <li>A simpler interface to touchInput - easy defaulting to default touch gesture behavior</li>
 *      <li>Uses suspend functions instead of callbacks (Kotlin coroutines)</li>
 *      <li>Use LatLon instead of LngLat</li>
 *  </ul>
 *  */
class KtMapController(private val c: MapController) {
    private val cameraManager = CameraManager(c)
    private val markerManager = MarkerManager(c)
    private val gestureManager = TouchGestureManager(c)

    private val defaultInterpolator = AccelerateDecelerateInterpolator()

    private val sceneUpdateContinuations = mutableMapOf<Int, Continuation<Int>>()
    private val pickLabelContinuations = mutableMapOf<PointF, Continuation<LabelPickResult?>>()
    private val featurePickContinuations = mutableMapOf<PointF, Continuation<Map<String, String>?>>()

    private var lastScenePath: String? = null
    private var lastSceneYaml: String? = null
    private var lastResourceRoot: String? = null
    private var lastSceneUpdates: List<SceneUpdate>? = null

    init {
        c.setSceneLoadListener { sceneId, sceneError ->
            val cont = sceneUpdateContinuations.remove(sceneId)
            if (sceneError != null) {
                cont?.resumeWithException(sceneError.toException())
            } else {
                markerManager.recreateMarkers()
                cont?.resume(sceneId)
            }
        }

        c.setLabelPickListener { labelPickResult: LabelPickResult?, positionX, positionY ->
            val p = PointF(positionX, positionY)
            pickLabelContinuations.remove(p)?.resume(labelPickResult)
        }

        c.setFeaturePickListener { properties: Map<String, String>?, positionX, positionY ->
            val p = PointF(positionX, positionY)
            featurePickContinuations.remove(p)?.resume(properties)
        }
    }

    /* ----------------------------- Loading and Updating Scene --------------------------------- */

    suspend fun loadSceneFile(path: String, sceneUpdates: List<SceneUpdate>? = null): Int {
        lastSceneYaml = null
        lastResourceRoot = null
        lastScenePath = path
        lastSceneUpdates = sceneUpdates

        return internalLoadSceneFile(path, sceneUpdates)
    }

    suspend fun loadSceneYaml(yaml: String, resourceRoot: String, sceneUpdates: List<SceneUpdate>? = null): Int {
        lastScenePath = null
        lastSceneYaml = yaml
        lastResourceRoot = resourceRoot
        lastSceneUpdates = sceneUpdates

        return internalLoadSceneYaml(yaml, resourceRoot, sceneUpdates)
    }

    suspend fun updateScene(sceneUpdates: List<SceneUpdate>): Int? {
        val path = lastScenePath
        val yaml = lastSceneYaml
        val resourceRoot = lastResourceRoot

        val newSceneUpdates = lastSceneUpdates.orEmpty() + sceneUpdates
        lastSceneUpdates = newSceneUpdates

        return if (path != null) {
            internalLoadSceneFile(path, newSceneUpdates)
        } else if(yaml != null && resourceRoot != null) {
            internalLoadSceneYaml(yaml, resourceRoot, newSceneUpdates)
        } else {
            null
        }
    }

    private suspend fun internalLoadSceneFile(
        path: String,
        sceneUpdates: List<SceneUpdate>? = null
    ): Int = suspendCoroutine { cont ->
        markerManager.invalidateMarkers()
        val sceneId = c.loadSceneFileAsync(path, sceneUpdates)
        sceneUpdateContinuations[sceneId] = cont
    }

    private suspend fun internalLoadSceneYaml(
        yaml: String,
        resourceRoot: String,
        sceneUpdates: List<SceneUpdate>? = null
    ): Int = suspendCoroutine { cont ->
        markerManager.invalidateMarkers()
        val sceneId = c.loadSceneYamlAsync(yaml, resourceRoot, sceneUpdates)
        sceneUpdateContinuations[sceneId] = cont
    }

    /* ----------------------------------------- Camera ----------------------------------------- */

    val cameraPosition: CameraPosition get() = cameraManager.camera

    fun updateCameraPosition(duration: Long = 0, interpolator: Interpolator = defaultInterpolator, builder: (CameraUpdate) -> Unit) {
        cameraManager.updateCamera(duration, interpolator, builder)
    }

    fun cancelAllCameraAnimations() = cameraManager.cancelAllCameraAnimations()

    var cameraType: MapController.CameraType
        set(value) { c.cameraType = value }
        get() = c.cameraType

    var minimumZoomLevel: Float
        set(value) { c.minimumZoomLevel = value }
        get() = c.minimumZoomLevel

    var maximumZoomLevel: Float
        set(value) { c.maximumZoomLevel = value }
        get() = c.maximumZoomLevel

    fun screenPositionToLatLon(screenPosition: PointF): LatLon? = c.screenPositionToLngLat(screenPosition)?.toLatLon()
    fun latLonToScreenPosition(latLon: LatLon): PointF = c.lngLatToScreenPosition(latLon.toLngLat())

    fun screenAreaToBoundingBox(padding: RectF): BoundingBox? {
        val view = glViewHolder?.view ?: return null
        val w = view.width
        val h = view.height
        if (w == 0 || h == 0) return null

        val size = PointF(w - padding.left - padding.right, h - padding.top - padding.bottom)

        // the special cases here are: map tilt and map rotation:
        // * map tilt makes the screen area -> world map area into a trapezoid
        // * map rotation makes the screen area -> world map area into a rotated rectangle
        // dealing with tilt: this method is just not defined if the tilt is above a certain limit
        if (cameraPosition.tilt > Math.PI / 4f) return null // 45°

        val positions = arrayOf(
            screenPositionToLatLon(PointF(padding.left, padding.top)),
            screenPositionToLatLon(PointF(padding.left + size.x, padding.top)),
            screenPositionToLatLon(PointF(padding.left, padding.top + size.y)),
            screenPositionToLatLon(PointF(padding.left + size.x, padding.top + size.y))
        ).filterNotNull()

        return enclosingBoundingBox(positions)
    }

    fun getMaxZoomThatContainsGeometry(geometry: ElementGeometry, padding: RectF): Float? {
        val objectBounds: BoundingBox = geometry.getBounds()
        val screenBounds: BoundingBox
        val currentZoom: Float
        synchronized(this) {
            screenBounds = screenAreaToBoundingBox(padding) ?: return null
            currentZoom = cameraPosition.zoom
        }
        val screenWidth = normalizeLongitude(screenBounds.maxLongitude - screenBounds.minLongitude)
        val screenHeight = screenBounds.maxLatitude - screenBounds.minLatitude
        val objectWidth = normalizeLongitude(objectBounds.maxLongitude - objectBounds.minLongitude)
        val objectHeight = objectBounds.maxLatitude - objectBounds.minLatitude
        val zoomDeltaX = log10(screenWidth / objectWidth) / log10(2.0)
        val zoomDeltaY = log10(screenHeight / objectHeight) / log10(2.0)
        return max( 1.0, currentZoom + min(zoomDeltaX, zoomDeltaY)).toFloat()
    }

    fun getLatLonThatCentersGeometry(geometry: ElementGeometry, padding: RectF): LatLon? {
        val view = glViewHolder?.view ?: return null
        val w = view.width
        val h = view.height
        if (w == 0 || h == 0) return null

        val normalCenter = screenPositionToLatLon(PointF(w / 2f, h / 2f)) ?: return null
        val offsetCenter = screenPositionToLatLon(
            PointF(
                padding.left + (w - padding.left - padding.right) / 2,
                padding.top + (h - padding.top - padding.bottom) / 2
            )
        ) ?: return null
        return OsmLatLon(
            geometry.center.latitude - offsetCenter.latitude - normalCenter.latitude,
            normalizeLongitude(geometry.center.longitude - offsetCenter.longitude - normalCenter.longitude)
        )
    }

    /* -------------------------------------- Data Layers --------------------------------------- */

    fun addDataLayer(name: String, generateCentroid: Boolean = false): MapData =
        c.addDataLayer(name, generateCentroid)

    /* ---------------------------------------- Markers ----------------------------------------- */

    fun addMarker(): Marker = markerManager.addMarker()
    fun removeMarker(marker: Marker): Boolean = removeMarker(marker.markerId)
    fun removeMarker(markerId: Long): Boolean = markerManager.removeMarker(markerId)
    fun removeAllMarkers() = markerManager.removeAllMarkers()

    /* ------------------------------------ Map interaction ------------------------------------- */

    fun setPickRadius(radius: Float) = c.setPickRadius(radius)

    suspend fun pickLabel(posX: Float, posY: Float): LabelPickResult? = suspendCoroutine { cont ->
        pickLabelContinuations[PointF(posX, posY)] = cont
        c.pickLabel(posX, posY)
    }

    suspend fun pickMarker(posX: Float, posY: Float): MarkerPickResult? = markerManager.pickMarker(posX, posY)

    suspend fun pickFeature(posX: Float, posY: Float): Map<String, String>? = suspendCoroutine { cont ->
        featurePickContinuations[PointF(posX, posY)] = cont
        c.pickFeature(posX, posY)
    }

    fun setMapChangeListener(listener: MapChangeListener?) = c.setMapChangeListener(listener)

    // TODO interaction listener



    /* -------------------------------------- Touch input --------------------------------------- */

    fun setShoveResponder(responder: TouchInput.ShoveResponder?) { gestureManager.setShoveResponder(responder) }
    fun setScaleResponder(responder: TouchInput.ScaleResponder?) { gestureManager.setScaleResponder(responder) }
    fun setRotateResponder(responder: TouchInput.RotateResponder?) { gestureManager.setRotateResponder(responder) }
    fun setPanResponder(responder: TouchInput.PanResponder?) { gestureManager.setPanResponder(responder) }
    fun setTapResponder(responder: TouchInput.TapResponder?) { c.touchInput.setTapResponder(responder) }
    fun setDoubleTapResponder(responder: TouchInput.DoubleTapResponder?) { c.touchInput.setDoubleTapResponder(responder) }
    fun setLongPressResponder(responder: TouchInput.LongPressResponder?) { c.touchInput.setLongPressResponder(responder) }

    fun isGestureEnabled(g: TouchInput.Gestures): Boolean = c.touchInput.isGestureEnabled(g)
    fun setGestureEnabled(g: TouchInput.Gestures) { c.touchInput.setGestureEnabled(g) }
    fun setGestureDisabled(g: TouchInput.Gestures) { c.touchInput.setGestureDisabled(g) }
    fun setAllGesturesEnabled() { c.touchInput.setAllGesturesEnabled() }
    fun setAllGesturesDisabled() { c.touchInput.setAllGesturesDisabled() }

    fun setSimultaneousDetectionEnabled(first: TouchInput.Gestures, second: TouchInput.Gestures) {
        c.touchInput.setSimultaneousDetectionEnabled(first, second)
    }
    fun setSimultaneousDetectionDisabled(first: TouchInput.Gestures, second: TouchInput.Gestures) {
        c.touchInput.setSimultaneousDetectionDisabled(first, second)
    }
    fun isSimultaneousDetectionAllowed(first: TouchInput.Gestures, second: TouchInput.Gestures): Boolean =
        c.touchInput.isSimultaneousDetectionAllowed(first, second)

    /* ------------------------------------------ Misc ------------------------------------------ */

    suspend fun captureFrame(waitForCompleteView: Boolean): Bitmap = suspendCoroutine { cont ->
        c.captureFrame({ bitmap -> cont.resume(bitmap) }, waitForCompleteView)
    }

    fun requestRender() = c.requestRender()
    fun setRenderMode(renderMode: Int) = c.setRenderMode(renderMode)

    fun queueEvent(block: () -> Unit) = c.queueEvent(block)

    val glViewHolder: GLViewHolder? get() = c.glViewHolder

    fun setDebugFlag(flag: MapController.DebugFlag, on: Boolean) = c.setDebugFlag(flag, on)

    fun useCachedGlState(use: Boolean) = c.useCachedGlState(use)

    fun setDefaultBackgroundColor(red: Float, green: Float, blue: Float) = c.setDefaultBackgroundColor(red, green, blue)
}

class LoadSceneException(message: String, val sceneUpdate: SceneUpdate) : RuntimeException(message)

private fun SceneError.toException() =
    LoadSceneException(error.name.toLowerCase(Locale.US).replace("_", " "), sceneUpdate)


suspend fun MapView.initMap(
    httpHandler: HttpHandler? = null,
    glViewHolderFactory: GLViewHolderFactory = GLSurfaceViewHolderFactory()) =

    suspendCoroutine<KtMapController?> { cont ->
        getMapAsync(MapView.MapReadyCallback { mapController ->
            cont.resume(mapController?.let {
                KtMapController(it)
            })
        }, glViewHolderFactory, httpHandler)
    }

