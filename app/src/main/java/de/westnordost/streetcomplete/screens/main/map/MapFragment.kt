package de.westnordost.streetcomplete.screens.main.map

import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.FragmentMapBinding
import de.westnordost.streetcomplete.screens.main.map.components.SceneMapComponent
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.getMetersPerPixel
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Manages a map that remembers its last location*/
open class MapFragment : Fragment() {

    private val binding by viewBinding(FragmentMapBinding::bind)

    protected var mapboxMap : MapboxMap? = null
    protected var sceneMapComponent: SceneMapComponent? = null

    private var previousCameraPosition: CameraPosition? = null

    var isMapInitialized: Boolean = false
        private set

    private val prefs: Preferences by inject()

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double)
        /** Called after camera animation or after the map was controlled by a user */
        fun onMapDidChange(position: LatLon, rotation: Double, tilt: Double, zoom: Double)
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
        Mapbox.getInstance(requireContext())
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapView = view.findViewById<MapView>(R.id.map)
        mapView.onCreate(savedInstanceState)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val mapFile = if (isNightMode) "map_theme/streetcomplete-night.json" else "map_theme/streetcomplete.json"
        val styleJsonString = resources.assets.open(mapFile).reader().readText()
        mapView.getMapAsync { map ->
            val s = Style.Builder().fromJson(styleJsonString)
            map.setStyle(s)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isMapInitialized = false
        binding.map.onCreate(savedInstanceState)

        binding.openstreetmapLink.setOnClickListener { showOpenUrlDialog("https://www.openstreetmap.org/copyright") }
        binding.mapTileProviderLink.text = "© JawgMaps"
        binding.mapTileProviderLink.setOnClickListener { showOpenUrlDialog("https://www.jawg.io") }

        binding.attributionContainer.respectSystemInsets(View::setMargins)

        val mapView = binding.map
        mapView.getMapAsync {
            map -> viewLifecycleScope.launch { initMap(mapView, map) }
        }
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
        mapboxMap = null
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.removeListener(Prefs.THEME_BACKGROUND, onThemeBackgroundChanged)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    /* ------------------------------------------- Map  ----------------------------------------- */

    private suspend fun initMap(mapView: MapView, mapboxMap: MapboxMap) {
        val style = mapboxMap.style ?: return
        registerResponders(mapboxMap)
        mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
            override fun onMoveBegin(p0: MoveGestureDetector) {
                // tapping also calls onMoveBegin, but with integer x and y, and with historySize 0
                if (p0.currentEvent.historySize != 0) // crappy workaround for deciding whether it's a tap or a move
                    listener?.onPanBegin()
            }
            override fun onMove(p0: MoveGestureDetector) {}
            override fun onMoveEnd(p0: MoveGestureDetector) {}
        })

        sceneMapComponent = SceneMapComponent(resources, mapboxMap)
        sceneMapComponent?.isAerialView = (prefs.getStringOrNull(Prefs.THEME_BACKGROUND) ?: "MAP") == "AERIAL"

        onBeforeLoadScene()

        sceneMapComponent?.loadScene()

//        ctrl.glViewHolder!!.view.awaitLayout()

        onMapReady(mapView, mapboxMap, style)

        isMapInitialized = true
        listener?.onMapInitialized()
    }

    private fun registerResponders(map: MapboxMap) {
        map.addOnCameraMoveListener {
            val camera = cameraPosition ?: return@addOnCameraMoveListener
            if (camera == previousCameraPosition) return@addOnCameraMoveListener
            previousCameraPosition = camera
            onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
            listener?.onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
        }
        map.addOnCameraIdleListener {
            val camera = cameraPosition ?: return@addOnCameraIdleListener
            if (camera == previousCameraPosition) return@addOnCameraIdleListener
            previousCameraPosition = camera
            onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom)
            listener?.onMapDidChange(camera.position, camera.rotation, camera.tilt, camera.zoom)
        }
    }

    /* ----------------------------- Overridable map callbacks --------------------------------- */

    @CallSuper protected open suspend fun onMapReady(
        mapView: MapView,
        mapboxMap: MapboxMap,
        style: Style
    ) {
        this.mapboxMap = mapboxMap
        restoreMapState()
    }

    @CallSuper protected open suspend fun onBeforeLoadScene() {}

    protected open fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {}

    protected open fun onMapDidChange(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {}

    /* ---------------------- Overridable callbacks for map interaction ------------------------ */
/*
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
*/
    fun onLongPress(x: Float, y: Float) {
        listener?.onLongPress(x, y)
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val camera = loadCameraPosition() ?: return
        mapboxMap?.camera = camera
    }

    private fun saveMapState() {
        val camera = mapboxMap?.camera ?: return
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
            prefs.getFloat(Prefs.MAP_ROTATION, 0f).toDouble(),
            prefs.getFloat(Prefs.MAP_TILT, 0f).toDouble(),
            prefs.getFloat(Prefs.MAP_ZOOM, 0f).toDouble()
        )
    }

    private fun saveCameraPosition(camera: CameraPosition) {
        prefs.putFloat(Prefs.MAP_ROTATION, camera.rotation.toFloat())
        prefs.putFloat(Prefs.MAP_TILT, camera.tilt.toFloat())
        prefs.putFloat(Prefs.MAP_ZOOM, camera.zoom.toFloat())
        prefs.putDouble(Prefs.MAP_LATITUDE, camera.position.latitude)
        prefs.putDouble(Prefs.MAP_LONGITUDE, camera.position.longitude)
    }

    /* ------------------------------- Controlling the map -------------------------------------- */

    fun getPositionAt(point: PointF): LatLon? =
        mapboxMap?.projection?.fromScreenLocation(point)?.toLatLon()

    fun getPointOf(pos: LatLon): PointF? =
        mapboxMap?.projection?.toScreenLocation(pos.toLatLng())

    val cameraPosition: CameraPosition?
        get() = mapboxMap?.camera

    fun updateCameraPosition(
        duration: Int = 0,
        builder: CameraUpdate.() -> Unit
    ) {
        mapboxMap?.updateCamera(duration, requireContext().contentResolver, builder)
    }

    fun setInitialCameraPosition(camera: CameraPosition) {
        if (mapboxMap != null) {
            mapboxMap?.camera = camera
        } else {
            saveCameraPosition(camera)
        }
    }

    fun getDisplayedArea(): BoundingBox? = mapboxMap?.screenAreaToBoundingBox()

    fun getMetersPerPixel(): Double? = mapboxMap?.getMetersPerPixel()
}
