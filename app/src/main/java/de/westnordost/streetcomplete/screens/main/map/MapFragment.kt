package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mapzen.tangram.TouchInput.DoubleTapResponder
import com.mapzen.tangram.TouchInput.LongPressResponder
import com.mapzen.tangram.TouchInput.PanResponder
import com.mapzen.tangram.TouchInput.RotateResponder
import com.mapzen.tangram.TouchInput.ScaleResponder
import com.mapzen.tangram.TouchInput.ShoveResponder
import com.mapzen.tangram.TouchInput.TapResponder
import com.mapzen.tangram.networking.DefaultHttpHandler
import com.mapzen.tangram.networking.HttpHandler
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloadCacheConfig
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.FragmentMapBinding
import de.westnordost.streetcomplete.screens.main.map.components.SceneMapComponent
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.MapChangingListener
import de.westnordost.streetcomplete.screens.main.map.tangram.initMap
import de.westnordost.streetcomplete.util.ktx.awaitLayout
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.Version
import org.koin.android.ext.android.inject

/** Manages a map that remembers its last location*/
open class MapFragment :
    Fragment(),
    TapResponder,
    DoubleTapResponder,
    LongPressResponder,
    PanResponder,
    ScaleResponder,
    ShoveResponder,
    RotateResponder {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private val defaultCameraInterpolator = AccelerateDecelerateInterpolator()

    protected var controller: KtMapController? = null
    protected var sceneMapComponent: SceneMapComponent? = null

    private var previousCameraPosition: CameraPosition? = null

    var isMapInitialized: Boolean = false
        private set

    private val hide3DBuildingsSceneUpdates = listOf(
        "layers.buildings.draw.buildings-style.extrude" to "false",
        "layers.buildings.draw.buildings-outline-style.extrude" to "false"
    )
    var show3DBuildings: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (sceneMapComponent?.isAerialView == true) return

            if (value) {
                sceneMapComponent?.removeSceneUpdates(hide3DBuildingsSceneUpdates)
            } else {
                sceneMapComponent?.addSceneUpdates(hide3DBuildingsSceneUpdates)
            }

            viewLifecycleScope.launch { sceneMapComponent?.loadScene() }
        }

    private val vectorTileProvider: VectorTileProvider by inject()
    private val cacheConfig: MapTilesDownloadCacheConfig by inject()
    private val prefs: Preferences by inject()

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float)
        /** Called after camera animation or after the map was controlled by a user */
        fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float)
        /** Called when the user begins to pan the map */
        fun onPanBegin()
        /** Called when the user long-presses the map */
        fun onLongPress(x: Float, y: Float)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val onThemeBackgroundChanged = {
        sceneMapComponent?.isAerialView =
            (prefs.getStringOrNull(Prefs.THEME_BACKGROUND) ?: "MAP") == "AERIAL"
    }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs.addListener(Prefs.THEME_BACKGROUND, onThemeBackgroundChanged)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isMapInitialized = false
        binding.map.onCreate(savedInstanceState)

        binding.openstreetmapLink.setOnClickListener { showOpenUrlDialog("https://www.openstreetmap.org/copyright") }
        binding.mapTileProviderLink.text = vectorTileProvider.copyrightText
        binding.mapTileProviderLink.setOnClickListener { showOpenUrlDialog(vectorTileProvider.copyrightLink) }

        binding.attributionContainer.respectSystemInsets(View::setMargins)

        viewLifecycleScope.launch { initMap() }
    }

    private fun showOpenUrlDialog(url: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.open_url)
            .setMessage(url)
            .setPositiveButton(android.R.string.ok) { _, _ -> openUri(url) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        viewLifecycleScope.launch {
            /* delay reloading of the scene a bit because if the language changed, the container
               activity will actually want to restart. onStart however is still called in that
               case */
            delay(50)
            sceneMapComponent?.loadScene()
        }
        binding.map.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onPause()
        saveMapState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.map.onDestroy()
        controller = null
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.removeListener(Prefs.THEME_BACKGROUND, onThemeBackgroundChanged)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        try {
            binding.map.onLowMemory()
        } catch (e: Exception) {
            // ignore (see https://github.com/streetcomplete/StreetComplete/issues/4221)
        }
    }

    /* ------------------------------------------- Map  ----------------------------------------- */

    private suspend fun initMap() {
        val ctrl = binding.map.initMap(createHttpHandler())
        controller = ctrl
        if (ctrl == null) return
        lifecycle.addObserver(ctrl)
        registerResponders(ctrl)

        sceneMapComponent = SceneMapComponent(resources, ctrl, vectorTileProvider)
        sceneMapComponent?.isAerialView = (prefs.getStringOrNull(Prefs.THEME_BACKGROUND) ?: "MAP") == "AERIAL"

        onBeforeLoadScene()

        sceneMapComponent?.loadScene()

        ctrl.glViewHolder!!.view.awaitLayout()

        onMapReady()

        isMapInitialized = true
        listener?.onMapInitialized()
    }

    private fun registerResponders(ctrl: KtMapController) {
        ctrl.setTapResponder(this)
        ctrl.setDoubleTapResponder(this)
        ctrl.setLongPressResponder(this)
        ctrl.setRotateResponder(this)
        ctrl.setPanResponder(this)
        ctrl.setScaleResponder(this)
        ctrl.setShoveResponder(this)
        ctrl.setMapChangingListener(object : MapChangingListener {
            override fun onMapWillChange() {}
            override fun onMapIsChanging() {
                val camera = cameraPosition ?: return
                if (camera == previousCameraPosition) return
                previousCameraPosition = camera
                onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
                listener?.onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
            }
            override fun onMapDidChange() {
                val camera = cameraPosition ?: return
                if (camera == previousCameraPosition) return
                previousCameraPosition = camera
                onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom)
                listener?.onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom)
            }
        })
    }

    private fun createHttpHandler(): HttpHandler {
        val builder = OkHttpClient.Builder().cache(cacheConfig.cache)
        return object : DefaultHttpHandler(builder) {
            override fun configureRequest(url: HttpUrl, builder: Request.Builder) {
                builder
                    .cacheControl(cacheConfig.tangramCacheControl)
                    .header("User-Agent", ApplicationConstants.USER_AGENT + " / " + Version.userAgent())
            }
        }
    }

    /* ----------------------------- Overridable map callbacks --------------------------------- */

    @CallSuper protected open suspend fun onMapReady() {
        restoreMapState()
    }

    @CallSuper protected open suspend fun onBeforeLoadScene() {}

    protected open fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {}

    protected open fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {}

    /* ---------------------- Overridable callbacks for map interaction ------------------------ */

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

    override fun onDoubleTap(x: Float, y: Float): Boolean {
        val pos = controller?.screenPositionToLatLon(PointF(x, y))
        if (pos != null) {
            controller?.updateCameraPosition(300L) {
                zoomBy = 1f
                position = pos
            }
        }
        return true
    }

    override fun onLongPress(x: Float, y: Float) {
        listener?.onLongPress(x, y)
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val camera = loadCameraPosition() ?: return
        controller?.setCameraPosition(camera)
    }

    private fun saveMapState() {
        val camera = controller?.cameraPosition ?: return
        saveCameraPosition(camera)
    }

    private fun loadCameraPosition(): CameraPosition? {
        if (!prefs.keys.containsAll(listOf(
                Prefs.MAP_LATITUDE,
                Prefs.MAP_LONGITUDE,
                Prefs.MAP_ROTATION,
                Prefs.MAP_TILT,
                Prefs.MAP_ZOOM,
        ))) {
            return null
        }

        return CameraPosition(
            LatLon(
                prefs.getDouble(Prefs.MAP_LATITUDE, 0.0),
                prefs.getDouble(Prefs.MAP_LONGITUDE, 0.0)
            ),
            prefs.getFloat(Prefs.MAP_ROTATION, 0f),
            prefs.getFloat(Prefs.MAP_TILT, 0f),
            prefs.getFloat(Prefs.MAP_ZOOM, 0f)
        )
    }

    private fun saveCameraPosition(camera: CameraPosition) {
        prefs.putFloat(Prefs.MAP_ROTATION, camera.rotation)
        prefs.putFloat(Prefs.MAP_TILT, camera.tilt)
        prefs.putFloat(Prefs.MAP_ZOOM, camera.zoom)
        prefs.putDouble(Prefs.MAP_LATITUDE, camera.position.latitude)
        prefs.putDouble(Prefs.MAP_LONGITUDE, camera.position.longitude)
    }

    /* ------------------------------- Controlling the map -------------------------------------- */

    fun adjustToOffsets(oldOffset: RectF, newOffset: RectF) {
        controller?.screenCenterToLatLon(oldOffset)?.let { pos ->
            controller?.updateCameraPosition {
                position = controller?.getLatLonThatCentersLatLon(pos, newOffset)
            }
        }
    }

    fun getPositionAt(point: PointF): LatLon? = controller?.screenPositionToLatLon(point)

    fun getPointOf(pos: LatLon): PointF? = controller?.latLonToScreenPosition(pos)

    fun getClippedPointOf(pos: LatLon): PointF? {
        val screenPositionOut = PointF()
        controller?.latLonToScreenPosition(pos, screenPositionOut, true) ?: return null
        return screenPositionOut
    }

    val cameraPosition: CameraPosition?
        get() = controller?.cameraPosition

    fun updateCameraPosition(
        duration: Long = 0,
        interpolator: Interpolator = defaultCameraInterpolator,
        builder: CameraUpdate.() -> Unit
    ) {
        controller?.updateCameraPosition(duration, interpolator, builder)
    }

    fun setInitialCameraPosition(camera: CameraPosition) {
        val controller = controller
        if (controller != null) {
            controller.setCameraPosition(camera)
        } else {
            saveCameraPosition(camera)
        }
    }

    fun getPositionThatCentersPosition(pos: LatLon, offset: RectF): LatLon? {
        return controller?.getLatLonThatCentersLatLon(pos, offset)
    }

    fun getDisplayedArea(): BoundingBox? = controller?.screenAreaToBoundingBox(RectF())

    fun getMetersPerPixel(): Double? {
        val view = view ?: return null
        val x = view.width / 2f
        val y = view.height / 2f
        val pos1 = controller?.screenPositionToLatLon(PointF(x, y)) ?: return null
        val pos2 = controller?.screenPositionToLatLon(PointF(x + 1, y)) ?: return null
        return pos1.distanceTo(pos2)
    }
}
