package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mapbox.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.FragmentMapBinding
import de.westnordost.streetcomplete.screens.main.map.components.SceneMapComponent
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitGetMap
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.getMetersPerPixel
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.maplibre.android.MapLibre

/** Manages a map that remembers its last location*/
open class MapFragment : Fragment(R.layout.fragment_map) {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private var mapLibreMap : MapLibreMap? = null
    private var sceneMapComponent: SceneMapComponent? = null

    private var previousCameraPosition: CameraPosition? = null

    private val prefs: Preferences by inject()

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double)
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
        MapLibre.getInstance(requireContext())
        prefs.addListener(Prefs.THEME_BACKGROUND, onThemeBackgroundChanged)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.map.onCreate(savedInstanceState)

        binding.openstreetmapLink.setOnClickListener { showOpenUrlDialog("https://www.openstreetmap.org/copyright") }
        binding.mapTileProviderLink.text = "© JawgMaps"
        binding.mapTileProviderLink.setOnClickListener { showOpenUrlDialog("https://www.jawg.io") }

        binding.attributionContainer.respectSystemInsets(View::setMargins)

        viewLifecycleScope.launch {
            val map = binding.map.awaitGetMap()
            mapLibreMap = map
            initMap(map)
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
        // sceneMapComponent might actually be null if map style not initialized yet
        sceneMapComponent?.updateStyle()
        binding.map.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onPause()
        saveMapState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapLibreMap = null
        binding.map.onDestroy()
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

    private suspend fun initMap(map: MapLibreMap) {
        map.addOnMoveListener(object : MapLibreMap.OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                // tapping also calls onMoveBegin, but with integer x and y, and with historySize 0
                if (detector.currentEvent.historySize != 0) // crappy workaround for deciding whether it's a tap or a move
                    listener?.onPanBegin()
            }
            override fun onMove(detector: MoveGestureDetector) {}
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        })
        map.addOnCameraMoveListener {
            val camera = cameraPosition ?: return@addOnCameraMoveListener
            if (camera == previousCameraPosition) return@addOnCameraMoveListener
            previousCameraPosition = camera
            onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
            listener?.onMapIsChanging(camera.position, camera.rotation, camera.tilt, camera.zoom)
        }
        map.addOnMapLongClickListener { pos ->
            val screenPoint: PointF = map.projection.toScreenLocation(pos)
            onLongPress(screenPoint.x, screenPoint.y)
            true
        }

        val sceneMapComponent = SceneMapComponent(requireContext(), map)
        sceneMapComponent.isAerialView = (prefs.getStringOrNull(Prefs.THEME_BACKGROUND) ?: "MAP") == "AERIAL"

        val style = sceneMapComponent.loadStyle()
        this.sceneMapComponent = sceneMapComponent

        onMapReady(map, style)

        listener?.onMapInitialized()
    }

    /* ----------------------------- Overridable map callbacks --------------------------------- */

    @CallSuper protected open suspend fun onMapReady(map: MapLibreMap, style: Style) {
        restoreMapState()
    }

    protected open fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {}

    /* ---------------------- Overridable callbacks for map interaction ------------------------ */

    open fun onLongPress(x: Float, y: Float) {
        listener?.onLongPress(x, y)
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val camera = loadCameraPosition() ?: return
        mapLibreMap?.camera = camera
    }

    private fun saveMapState() {
        val camera = mapLibreMap?.camera ?: return
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
        mapLibreMap?.projection?.fromScreenLocation(point)?.toLatLon()

    fun getPointOf(pos: LatLon): PointF? =
        mapLibreMap?.projection?.toScreenLocation(pos.toLatLng())

    val cameraPosition: CameraPosition?
        get() = mapLibreMap?.camera

    fun updateCameraPosition(
        duration: Int = 0,
        builder: CameraUpdate.() -> Unit
    ) {
        mapLibreMap?.updateCamera(duration, requireContext().contentResolver, builder)
    }

    fun setInitialCameraPosition(camera: CameraPosition) {
        if (mapLibreMap != null) {
            mapLibreMap?.camera = camera
        } else {
            saveCameraPosition(camera)
        }
    }

    fun getDisplayedArea(): BoundingBox? = mapLibreMap?.screenAreaToBoundingBox()

    fun getMetersPerPixel(): Double? = mapLibreMap?.getMetersPerPixel()
}
