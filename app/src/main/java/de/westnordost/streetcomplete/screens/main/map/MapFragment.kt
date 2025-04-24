package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.FragmentMapBinding
import de.westnordost.streetcomplete.screens.main.map.components.SceneMapComponent
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraUpdate
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitGetMap
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.deleteRegionsOlderThan
import de.westnordost.streetcomplete.screens.main.map.maplibre.getMetersPerPixel
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.screens.main.map.maplibre.updateCamera
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.maplibre.android.MapLibre
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.offline.OfflineManager

/** Manages a map that remembers its last location */
open class MapFragment : Fragment(R.layout.fragment_map) {

    private val binding by viewBinding(FragmentMapBinding::bind)

    protected var map: MapLibreMap? = null
    private var sceneMapComponent: SceneMapComponent? = null

    private val prefs: Preferences by inject()

    private var started = true
    private var styleNeedsReload = false

    private val themeChangeListener: SettingsListener = prefs.prefs.addStringListener(Prefs.THEME_BACKGROUND, "MAP") {
        if (started)
            viewLifecycleScope.launch {
                if (it == "AERIAL")
                    // crappy workaround for a bug: when switching to raster background, the raster tile in current view is invisible
                    // so we zoom out and in again and hope the tile is loaded
                    updateCameraPosition { zoomBy = -1.0}
                reloadStyle()
                if (it == "AERIAL")
                    updateCameraPosition { zoomBy = 1.0}
            }
        else styleNeedsReload = true
    }

    interface Listener {
        /** Called when the map has been completely initialized */
        fun onMapInitialized()
        /** Called during camera animation and while the map is being controlled by a user */
        fun onMapIsChanging(camera: CameraPosition)
        /** Called when the user begins to pan the map */
        fun onPanBegin()
        /** Called when the user long-presses the map */
        fun onLongPress(point: PointF, position: LatLon)
        /** Called when user starts to move the map */
        fun onUserCameraMoveStarted()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    // Note: offline regions may exceed this limit, but will count against it
    // This means that when offline regions exceed size, no tiles will be cached when panning

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.map.onCreate(savedInstanceState)
        binding.map.foreground = view.context.getDrawable(R.color.background)

        initOfflineCacheSize()
        cleanOldOfflineRegions()

        viewLifecycleScope.launch {
            val map = binding.map.awaitGetMap()
            this@MapFragment.map = map
            initMap(map)
        }
    }

    private fun initOfflineCacheSize() {
        // set really high tile count limit
        OfflineManager.getInstance(requireContext()).setOfflineMapboxTileCountLimit(10000) // very roughly 1000 km²
    }

    private fun cleanOldOfflineRegions() {
        // the offline manager is only available together with the map, i.e. not from the CleanerWorker
        lifecycleScope.launch {
            delay(30000) // cleaning is low priority, do it once startup is done
            val retainTime = prefs.getInt(Prefs.DATA_RETAIN_TIME, ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS)
            val oldDataTimestamp = nowAsEpochMilliseconds() - retainTime
            OfflineManager.getInstance(requireContext()).deleteRegionsOlderThan(oldDataTimestamp)
        }
    }

    override fun onStart() {
        super.onStart()
        started = true
        // sceneMapComponent might actually be null if map style not initialized yet
        if (styleNeedsReload) viewLifecycleScope.launch { reloadStyle() }
        else sceneMapComponent?.updateStyle()
        styleNeedsReload = false
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onStop() {
        super.onStop()
        started = false
        saveMapState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map = null
        binding.map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    /* ------------------------------------------- Map  ----------------------------------------- */

    private suspend fun initMap(map: MapLibreMap) {
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isAttributionEnabled = false
        map.uiSettings.isLogoEnabled = false
        map.uiSettings.flingThreshold = 250
        map.uiSettings.flingAnimationBaseTime = 500
        map.uiSettings.isDisableRotateWhenScaling = !prefs.getBoolean(Prefs.ROTATE_WHILE_ZOOMING, false)

        // workaround for https://github.com/maplibre/maplibre-native/issues/2792
        map.gesturesManager.moveGestureDetector.moveThreshold = resources.dpToPx(5f)
        map.gesturesManager.rotateGestureDetector.angleThreshold = prefs.getFloat(Prefs.ROTATE_ANGLE_THRESHOLD, 1.5f)
        map.gesturesManager.shoveGestureDetector.pixelDeltaThreshold = resources.dpToPx(8f)

        map.addOnMoveListener(object : MapLibreMap.OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) { listener?.onPanBegin() }
            override fun onMove(detector: MoveGestureDetector) {}
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        })
        map.addOnCameraMoveListener {
            val camera = cameraPosition ?: return@addOnCameraMoveListener
            onMapIsChanging(camera)
            listener?.onMapIsChanging(camera)
        }
        map.addOnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                listener?.onUserCameraMoveStarted()
            }
        }
        map.addOnMapLongClickListener { pos ->
            onLongPress(map.projection.toScreenLocation(pos), pos.toLatLon())
            true
        }

        val sceneMapComponent = SceneMapComponent(requireContext(), map, prefs)
        val style = sceneMapComponent.loadStyle()
        this.sceneMapComponent = sceneMapComponent

        restoreMapState()
        binding.map.foreground = null

        onMapStyleLoaded(map, style)

        listener?.onMapInitialized()
    }

    @UiThread
    private suspend fun reloadStyle() {
        val map = map ?: return
        val sceneMapComponent = sceneMapComponent ?: return
        onMapStyleLoaded(map, sceneMapComponent.loadStyle())
    }

    /* ----------------------------- Overridable map callbacks --------------------------------- */

    protected open suspend fun onMapStyleLoaded(map: MapLibreMap, style: Style) {}

    protected open fun onMapIsChanging(camera: CameraPosition) {}

    /* ---------------------- Overridable callbacks for map interaction ------------------------ */

    open fun onLongPress(point: PointF, position: LatLon) {
        listener?.onLongPress(point, position)
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        map?.camera = loadCameraPosition()
    }

    private fun saveMapState() {
        val camera = map?.camera ?: return
        saveCameraPosition(camera)
    }

    private fun loadCameraPosition() = CameraPosition(
        position = prefs.mapPosition,
        rotation = prefs.mapRotation,
        tilt = prefs.mapTilt,
        zoom = prefs.mapZoom
    )

    private fun saveCameraPosition(camera: CameraPosition) {
        prefs.mapRotation = camera.rotation
        prefs.mapTilt = camera.tilt
        prefs.mapZoom = camera.zoom
        prefs.mapPosition = camera.position
    }

    /* ------------------------------- Controlling the map -------------------------------------- */

    fun getPositionAt(point: PointF): LatLon? =
        map?.projection?.fromScreenLocation(point)?.toLatLon()

    fun getPointOf(pos: LatLon): PointF? =
        map?.projection?.toScreenLocation(pos.toLatLng())

    val cameraPosition: CameraPosition?
        get() = map?.camera

    fun updateCameraPosition(duration: Int = 0, builder: CameraUpdate.() -> Unit) {
        map?.updateCamera(duration, requireContext().contentResolver, builder)
    }

    open fun setInitialCameraPosition(camera: CameraPosition) {
        if (map != null) {
            map?.camera = camera
        } else {
            saveCameraPosition(camera)
        }
    }

    fun getDisplayedArea(): BoundingBox? = map?.screenAreaToBoundingBox()

    fun getMetersPerPixel(): Double? = map?.getMetersPerPixel()
}
