package de.westnordost.streetcomplete.map

import android.app.Activity
import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mapzen.tangram.*
import com.mapzen.tangram.TouchInput.*
import com.mapzen.tangram.networking.HttpHandler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.BuildConfig.MAPZEN_API_KEY
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.map.tangram.*
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.CameraUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/** Manages a map that remembers its last location*/
open class MapFragment : Fragment(),
    CoroutineScope by CoroutineScope(Dispatchers.Main),
    TapResponder, DoubleTapResponder, LongPressResponder,
    PanResponder, ScaleResponder, ShoveResponder, RotateResponder {

    private lateinit var mapView: MapView

    protected var controller: KtMapController? = null

    private val defaultCameraInterpolator = AccelerateDecelerateInterpolator()

    private var loadedSceneFilePath: String? = null

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging()
        /** Called after camera animation or after the map was controlled by a user */
        fun onMapDidChange(animated: Boolean)
    }
    private val listener get() = parentFragment as? Listener ?: activity as? Listener

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        launch { initMap() }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        saveMapState()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
        mapView.onDestroy()
        controller = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /* ------------------------------------------- Map  ----------------------------------------- */

    private suspend fun initMap() {
        val ctrl = mapView.initMap(createHttpHandler())!!
        controller = ctrl
        registerResponders()
        onMapControllerReady()
        restoreMapState()

        val sceneFilePath = getSceneFilePath()
        ctrl.loadSceneFile(sceneFilePath, getSceneUpdates())
        loadedSceneFilePath = sceneFilePath
        onSceneReady()
        listener?.onMapInitialized()
    }

    private fun registerResponders() {
        controller?.setTapResponder(this)
        controller?.setDoubleTapResponder(this)
        controller?.setLongPressResponder(this)
        controller?.setRotateResponder(this)
        controller?.setPanResponder(this)
        controller?.setScaleResponder(this)
        controller?.setShoveResponder(this)


        controller?.setMapChangeListener(object : MapChangeListener {
            override fun onViewComplete() {}
            override fun onRegionWillChange(animated: Boolean) {}
            override fun onRegionIsChanging() {
                onMapIsChanging()
                listener?.onMapIsChanging()
            }
            override fun onRegionDidChange(animated: Boolean) {
                onMapDidChange(animated)
                listener?.onMapDidChange(animated)
            }
        })
    }

    protected open suspend fun getSceneUpdates(): List<SceneUpdate> {
        return listOf(SceneUpdate("global.language", Locale.getDefault().language))
    }

    protected open fun getSceneFilePath(): String {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val scene = if (isNightMode) "scene-dark.yaml" else "scene-light.yaml"
        return "map_theme/$scene"
    }

    private fun createHttpHandler(): HttpHandler {
        val cacheSize = PreferenceManager.getDefaultSharedPreferences(context).getLong(Prefs.MAP_TILECACHE, 50)
        val cacheDir = context!!.externalCacheDir
        val tileCacheDir: File?
        tileCacheDir = cacheDir?.let { File(cacheDir, "tile_cache") }
        return CachingHttpHandler(MAPZEN_API_KEY, tileCacheDir, (cacheSize * 1024 * 1024))
    }

    /* ----------------------------- Overrideable map callbacks --------------------------------- */

    protected open fun onMapControllerReady() {}

    protected open fun onSceneReady() {}

    protected open fun onMapIsChanging() {}

    protected open fun onMapDidChange(animated: Boolean) {}

    /* ---------------------- Overrideable callbacks for map interaction ------------------------ */

    override fun onPanBegin(): Boolean { return false }
    override fun onPan(startX: Float, startY: Float, endX: Float, endY: Float): Boolean { return false }
    override fun onPanEnd(): Boolean { return false }
    override fun onFling(posX: Float, posY: Float, velocityX: Float, velocityY: Float): Boolean { return false }
    override fun onCancelFling(): Boolean { return false }

    override fun onScaleBegin(): Boolean { return false }
    override fun onScale(x: Float, y: Float, scale: Float, velocity: Float): Boolean { return false }
    override fun onScaleEnd(): Boolean { return false }

    override fun onShoveBegin(): Boolean { return false }
    override fun onShove(distance: Float): Boolean { return false }
    override fun onShoveEnd(): Boolean { return false }

    override fun onRotateEnd(): Boolean { return false }
    override fun onRotate(x: Float, y: Float, rotation: Float): Boolean { return false }
    override fun onRotateBegin(): Boolean { return false }

    override fun onSingleTapUp(x: Float, y: Float): Boolean { return false }

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean { return false }

    override fun onDoubleTap(x: Float, y: Float): Boolean {
        val zoomTo = controller?.screenPositionToLatLon(PointF(x, y))
        if (zoomTo != null) {
            controller?.updateCameraPosition(500) {
                it.position = zoomTo
                it.zoomBy = 1.5f
            }
        }
        return true
    }
    override fun onLongPress(x: Float, y: Float) { }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val prefs = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        controller?.updateCameraPosition {
            if (prefs.contains(PREF_ROTATION)) it.rotation = prefs.getFloat(PREF_ROTATION, 0f)
            if (prefs.contains(PREF_TILT)) it.tilt = prefs.getFloat(PREF_TILT, 0f)
            if (prefs.contains(PREF_ZOOM)) it.zoom = prefs.getFloat(PREF_ZOOM, 0f)
            if (prefs.contains(PREF_LAT) && prefs.contains(PREF_LON)) {
                it.position = OsmLatLon(
                    java.lang.Double.longBitsToDouble(prefs.getLong(PREF_LAT, 0)),
                    java.lang.Double.longBitsToDouble(prefs.getLong(PREF_LON, 0))
                )
            }
        }
    }

    private fun saveMapState() {
        val camera = controller?.cameraPosition ?: return
        activity?.getPreferences(Activity.MODE_PRIVATE)?.edit {
            putFloat(PREF_ROTATION, camera.rotation)
            putFloat(PREF_TILT, camera.tilt)
            putFloat(PREF_ZOOM, camera.zoom)
            putLong(PREF_LAT, java.lang.Double.doubleToRawLongBits(camera.position.latitude))
            putLong(PREF_LON, java.lang.Double.doubleToRawLongBits(camera.position.longitude))
        }
    }

    /* ------------------------------- Controlling the map -------------------------------------- */

    fun getPositionAt(point: PointF): LatLon? = controller?.screenPositionToLatLon(point)

    fun getPointOf(pos: LatLon): PointF? = controller?.latLonToScreenPosition(pos)

    val cameraPosition: CameraPosition?
        get() = controller?.cameraPosition

    fun updateCameraPosition(
        duration: Long = 0,
        interpolator: Interpolator = defaultCameraInterpolator,
        builder: (CameraUpdate) -> Unit) {
        
        controller?.updateCameraPosition(duration, interpolator, builder)
    }

    fun getDisplayedArea(): BoundingBox? = controller?.screenAreaToBoundingBox(RectF())

    companion object {
        const val PREF_ROTATION = "map_rotation"
        const val PREF_TILT = "map_tilt"
        const val PREF_ZOOM = "map_zoom"
        const val PREF_LAT = "map_lat"
        const val PREF_LON = "map_lon"
    }
}
