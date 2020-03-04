package de.westnordost.streetcomplete.map

import android.app.Activity
import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.CallSuper
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mapzen.tangram.*
import com.mapzen.tangram.TouchInput.*
import com.mapzen.tangram.networking.DefaultHttpHandler
import com.mapzen.tangram.networking.HttpHandler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig.MAPZEN_API_KEY
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.awaitLayout
import de.westnordost.streetcomplete.ktx.containsAll
import de.westnordost.streetcomplete.map.tangram.*
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.CameraUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.internal.Version
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Exception

/** Manages a map that remembers its last location*/
open class MapFragment : Fragment(),
    CoroutineScope by CoroutineScope(Dispatchers.Main),
    TapResponder, DoubleTapResponder, LongPressResponder,
    PanResponder, ScaleResponder, ShoveResponder, RotateResponder {

    private lateinit var mapView: MapView

    protected var controller: KtMapController? = null

    private val defaultCameraInterpolator = AccelerateDecelerateInterpolator()

    private var loadedSceneFilePath: String? = null

    private var isMapInitialized: Boolean = false

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float)
        /** Called after camera animation or after the map was controlled by a user */
        fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float, animated: Boolean)
        /** Called when the user begins to pan the map */
        fun onPanBegin()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

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

    override fun onStart() {
        super.onStart()
        launch { reinitializeMapIfNecessary() }
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
        try {
            mapView.onDestroy()
        } catch (e : Exception) {
            // workaround for https://github.com/tangrams/tangram-es/issues/2136
            Log.e(TAG, "Error on disposing map", e)
        }
        controller = null
        coroutineContext.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /* ------------------------------------------- Map  ----------------------------------------- */

    private suspend fun initMap() {
        val ctrl = mapView.initMap(createHttpHandler())
        controller = ctrl
        if (ctrl == null) return
        registerResponders()

        val sceneFilePath = getSceneFilePath()
        ctrl.loadSceneFile(sceneFilePath, getSceneUpdates())
        loadedSceneFilePath = sceneFilePath

        ctrl.glViewHolder!!.view.awaitLayout()

        onMapReady()
        listener?.onMapInitialized()
    }

    private suspend fun reinitializeMapIfNecessary() {
        if (loadedSceneFilePath != null) {
            val sceneFilePath = getSceneFilePath()
            if (sceneFilePath != loadedSceneFilePath) {
                controller?.loadSceneFile(sceneFilePath, getSceneUpdates())
                loadedSceneFilePath = sceneFilePath
            }
        }
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
                val camera = cameraPosition ?: return
                onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
                listener?.onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
            }
            override fun onRegionDidChange(animated: Boolean) {
                val camera = cameraPosition ?: return
                onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom, animated)
                listener?.onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom, animated)
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
        val cacheSize = PreferenceManager.getDefaultSharedPreferences(context).getInt(Prefs.MAP_TILECACHE_IN_MB, 50)
        val cacheDir = context!!.externalCacheDir
        val tileCacheDir: File?
        if (cacheDir != null) {
            tileCacheDir = File(cacheDir, "tile_cache")
            if (!tileCacheDir.exists()) tileCacheDir.mkdir()
        } else {
            tileCacheDir = null
        }

        return object : DefaultHttpHandler() {

            val cacheControl = CacheControl.Builder().maxStale(7, TimeUnit.DAYS).build()

            override fun configureClient(builder: OkHttpClient.Builder) {
                if (tileCacheDir?.exists() == true) {
                    builder.cache(Cache(tileCacheDir, cacheSize * 1024L * 1024L))
                }
            }

            override fun configureRequest(url: HttpUrl, builder: Request.Builder) {
                if (MAPZEN_API_KEY != null)
                    builder.url(url.newBuilder().addQueryParameter("api_key", MAPZEN_API_KEY).build())

                builder
                    .cacheControl(cacheControl)
                    .header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent())
            }
        }
    }

    /* ----------------------------- Overrideable map callbacks --------------------------------- */

    @CallSuper protected open fun onMapReady() {
        restoreMapState()
    }

    protected open fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {}

    protected open fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float, animated: Boolean) {}

    /* ---------------------- Overrideable callbacks for map interaction ------------------------ */

    override fun onPanBegin(): Boolean {
        listener?.onPanBegin()
        return false
    }
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

    override fun onRotateBegin(): Boolean { return false }
    override fun onRotate(x: Float, y: Float, rotation: Float): Boolean { return false }
    override fun onRotateEnd(): Boolean { return false }

    override fun onSingleTapUp(x: Float, y: Float): Boolean { return false }

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean { return false }

    override fun onDoubleTap(x: Float, y: Float): Boolean { return false }

    override fun onLongPress(x: Float, y: Float) { }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val camera = loadCameraPosition() ?: return
        controller?.setCameraPosition(camera)
    }

    private fun saveMapState() {
        val camera = controller?.cameraPosition ?: return
        saveCameraPosition(camera, false)
    }

    private fun loadCameraPosition(): CameraPosition? {
        val prefs = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return null
        if (!prefs.containsAll(listOf(PREF_LAT, PREF_LON, PREF_ROTATION, PREF_TILT, PREF_ZOOM))) return null

        return CameraPosition(
            OsmLatLon(
                java.lang.Double.longBitsToDouble(prefs.getLong(PREF_LAT, 0)),
                java.lang.Double.longBitsToDouble(prefs.getLong(PREF_LON, 0))
            ),
            prefs.getFloat(PREF_ROTATION, 0f),
            prefs.getFloat(PREF_TILT, 0f),
            prefs.getFloat(PREF_ZOOM, 0f)
        )
    }

    private fun saveCameraPosition(camera: CameraPosition, saveNow: Boolean) {
        activity?.getPreferences(Activity.MODE_PRIVATE)?.edit(saveNow) {
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

    fun isPositionInView(pos: LatLon): Boolean {
        val controller = controller ?: return false
        val p = controller.latLonToScreenPosition(pos)
        return p.x >= 0 && p.y >= 0 && p.x < mapView.width && p.y < mapView.height
    }

    val cameraPosition: CameraPosition?
        get() = controller?.cameraPosition

    fun updateCameraPosition(
        duration: Long = 0,
        interpolator: Interpolator = defaultCameraInterpolator,
        builder: CameraUpdate.() -> Unit) {
        
        controller?.updateCameraPosition(duration, interpolator, builder)
    }

    fun setInitialCameraPosition(camera: CameraPosition) {
        val controller = controller
        if (controller != null) {
            controller.setCameraPosition(camera)
        } else {
            saveCameraPosition(camera, true)
        }
    }

    fun getPositionThatCentersPosition(pos: LatLon, offset: RectF): LatLon? {
        return controller?.getLatLonThatCentersLatLon(pos, offset)
    }

    fun getViewPosition(offset: RectF): LatLon? {
        return controller?.screenCenterToLatLon(offset)
    }

    var show3DBuildings: Boolean = true
    set(value) {
        if (field == value) return
        field = value
        launch {
            val sceneFile = loadedSceneFilePath
            if (sceneFile != null) {
                val toggle = if (value) "true" else "false"
                controller?.loadSceneFile(
                    sceneFile, getSceneUpdates() + listOf(
                        SceneUpdate("layers.buildings.draw.buildings-style.extrude", toggle),
                        SceneUpdate("layers.buildings.draw.buildings-outline-style.extrude", toggle)
                    )
                )
            }
        }
    }

    fun getDisplayedArea(): BoundingBox? = controller?.screenAreaToBoundingBox(RectF())

    companion object {
        const val PREF_ROTATION = "map_rotation"
        const val PREF_TILT = "map_tilt"
        const val PREF_ZOOM = "map_zoom"
        const val PREF_LAT = "map_lat"
        const val PREF_LON = "map_lon"

        private const val TAG = "MapFragment"
    }
}
