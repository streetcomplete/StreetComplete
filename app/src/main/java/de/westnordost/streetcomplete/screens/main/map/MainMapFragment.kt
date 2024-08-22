package de.westnordost.streetcomplete.screens.main.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.content.getSystemService
import androidx.core.graphics.Insets
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.CurrentLocationMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.DownloadedAreaMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.GeometryMarkersMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.SelectedPinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.TracksMapComponent
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.camera
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.util.ktx.currentDisplay
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.visibility
import kotlin.math.PI

/** This is the map shown in the main view. It manages a map that shows the quest pins, quest
 *  geometry, overlays, tracks, location... */
class MainMapFragment : MapFragment(), ShowsGeometryMarkers {

    private val questTypeOrderSource: QuestTypeOrderSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val editHistorySource: EditHistorySource by inject()
    private val mapDataSource: MapDataWithEditsSource by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()
    private val downloadedTilesSource: DownloadedTilesSource by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val recentLocationStore: RecentLocationStore by inject()
    private val prefs: Preferences by inject()

    private lateinit var compass: Compass
    private lateinit var locationManager: FineLocationManager

    private var mapImages: MapImages? = null
    private var geometryMarkersMapComponent: GeometryMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var selectedPinsMapComponent: SelectedPinsMapComponent? = null
    private var geometryMapComponent: FocusGeometryMapComponent? = null
    private var questPinsManager: QuestPinsManager? = null
    private var editHistoryPinsManager: EditHistoryPinsManager? = null
    private var styleableOverlayMapComponent: StyleableOverlayMapComponent? = null
    private var styleableOverlayManager: StyleableOverlayManager? = null
    private var downloadedAreaMapComponent: DownloadedAreaMapComponent? = null
    private var downloadedAreaManager: DownloadedAreaManager? = null
    private var locationMapComponent: CurrentLocationMapComponent? = null
    private var tracksMapComponent: TracksMapComponent? = null

    interface Listener {
        fun onClickedQuest(questKey: QuestKey)
        fun onClickedEdit(editKey: EditKey)
        fun onClickedElement(elementKey: ElementKey)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)

        /** Called after the map fragment updated its displayed location */
        fun onDisplayedLocationDidChange()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /** When the view follows the GPS position, whether the view already zoomed to the location once*/
    private var zoomedYet = false

    /** The GPS position at which the user is displayed at */
    var displayedLocation: Location? = null
        private set

    /** The GPS trackpoints the user has walked */
    private var tracks: ArrayList<ArrayList<Trackpoint>>

    private var _recordedTracks: ArrayList<Trackpoint>
    /** The GPS trackpoints the user has recorded */
    val recordedTracks: List<Trackpoint> get() = _recordedTracks

    /** If we are actively recording track history */
    var isRecordingTracks = false
        private set

    /** Whether the view should automatically center on the GPS location */
    var isFollowingPosition = true
        set(value) {
            if (!value) zoomedYet = false
            field = value
        }

    /** Whether the view should automatically rotate with bearing (like during navigation) */
    var isNavigationMode: Boolean = false
        set(value) {
            val valueChanged = field != value
            field = value
            if (valueChanged) onUpdatedNavigationMode()
        }

    enum class PinMode { NONE, QUESTS, EDITS }
    var pinMode: PinMode = PinMode.QUESTS
        set(value) {
            if (field == value) return
            field = value
            onUpdatedPinMode()
        }

    private var previouslyHiddenLayers: List<String> = emptyList()

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            this@MainMapFragment.onSelectedOverlayChanged()
        }
    }

    //region Lifecycle

    init {
        tracks = ArrayList()
        tracks.add(ArrayList())
        _recordedTracks = ArrayList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        compass = Compass(
            context.getSystemService<SensorManager>()!!,
            context.currentDisplay,
            this::onCompassRotationChanged
        )
        lifecycle.addObserver(compass)
        locationManager = FineLocationManager(context, this::onLocationChanged)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            displayedLocation = savedInstanceState.getParcelable(DISPLAYED_LOCATION)
            isRecordingTracks = savedInstanceState.getBoolean(TRACKS_IS_RECORDING)
            tracks = Json.decodeFromString(savedInstanceState.getString(TRACKS)!!)
        }
    }

    override fun onStart() {
        super.onStart()
        locationAvailabilityReceiver.addListener(::onLocationAvailabilityChanged)
        onLocationAvailabilityChanged(requireContext().isLocationAvailable)
    }

    override suspend fun onMapStyleLoaded(map: MapLibreMap, style: Style) {
        setupComponents(requireContext(), map, style)

        map.addOnMapClickListener(::onClickMap)

        setupLayers(style)

        setupData(map)
    }

    private fun setupComponents(context: Context, map: MapLibreMap, style: Style) {
        val fingerRadius = context.resources.dpToPx(CLICK_AREA_SIZE_IN_DP / 2)

        mapImages = MapImages(context.resources, style)

        geometryMarkersMapComponent = GeometryMarkersMapComponent(context, map, mapImages!!)

        locationMapComponent = CurrentLocationMapComponent(context, style, map)
        viewLifecycleOwner.lifecycle.addObserver(locationMapComponent!!)

        tracksMapComponent = TracksMapComponent(context, style, map)
        viewLifecycleOwner.lifecycle.addObserver(tracksMapComponent!!)

        pinsMapComponent = PinsMapComponent(context, context.contentResolver, map, mapImages!!, ::onClickPin)
        geometryMapComponent = FocusGeometryMapComponent(context.contentResolver, map)
        viewLifecycleOwner.lifecycle.addObserver(geometryMapComponent!!)

        styleableOverlayMapComponent = StyleableOverlayMapComponent(context, map, mapImages!!, fingerRadius, ::onClickElement)

        downloadedAreaMapComponent = DownloadedAreaMapComponent(context, map)

        selectedPinsMapComponent = SelectedPinsMapComponent(context, map, mapImages!!)
        viewLifecycleOwner.lifecycle.addObserver(selectedPinsMapComponent!!)
    }

    private fun setupLayers(style: Style) {
        // layers added first appear behind other layers

        // left-and-right lines should be rendered behind the actual road
        val firstCasingLayer = "pedestrian-tunnel-casing"
        for (layer in styleableOverlayMapComponent?.sideLayers.orEmpty()) {
            style.addLayerBelow(layer, firstCasingLayer)
        }
        val firstBridgeCasingLayer = "pedestrian-bridge-casing"
        for (layer in styleableOverlayMapComponent?.sideLayersBridge.orEmpty()) {
            style.addLayerBelow(layer, firstBridgeCasingLayer)
        }

        // labels should be on top of other layers
        val firstLabelLayer = "labels-country"
        for (layer in listOfNotNull(
            downloadedAreaMapComponent?.layers,
            styleableOverlayMapComponent?.layers,
            tracksMapComponent?.layers,
        ).flatten()) {
            style.addLayerBelow(layer, firstLabelLayer)
        }

        // these are always on top of everything else (including labels)
        for (layer in listOfNotNull(
            styleableOverlayMapComponent?.labelLayers,
            geometryMarkersMapComponent?.layers,
            geometryMapComponent?.layers,
            locationMapComponent?.layers,
            pinsMapComponent?.layers,
            selectedPinsMapComponent?.layers
        ).flatten()) {
            style.addLayer(layer)
        }
    }

    private fun setupData(map: MapLibreMap) {
        restoreMapState()
        centerCurrentPositionIfFollowing()

        questPinsManager = QuestPinsManager(map, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, visibleQuestsSource)
        questPinsManager!!.isVisible = pinMode == PinMode.QUESTS
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource)
        editHistoryPinsManager!!.isVisible = pinMode == PinMode.EDITS
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)

        styleableOverlayManager = StyleableOverlayManager(map, styleableOverlayMapComponent!!, mapDataSource, selectedOverlaySource)
        viewLifecycleOwner.lifecycle.addObserver(styleableOverlayManager!!)

        downloadedAreaManager = DownloadedAreaManager(downloadedAreaMapComponent!!, downloadedTilesSource)
        viewLifecycleOwner.lifecycle.addObserver(downloadedAreaManager!!)

        onSelectedOverlayChanged()
        selectedOverlaySource.addListener(overlayListener)

        locationMapComponent?.targetLocation = displayedLocation

        val positionsLists = tracks.map { track -> track.map { it.position } }
        tracksMapComponent?.setTracks(positionsLists, isRecordingTracks)
    }

    override fun onStop() {
        super.onStop()
        locationAvailabilityReceiver.removeListener(::onLocationAvailabilityChanged)
        saveMapState()
        stopPositionTracking()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(DISPLAYED_LOCATION, displayedLocation)
        // the amount of data one can put into a bundle is limited, let's cut off at 1000 points
        outState.putString(TRACKS, Json.encodeToString(tracks.takeLastNested(1000)))
        outState.putBoolean(TRACKS_IS_RECORDING, isRecordingTracks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedOverlaySource.removeListener(overlayListener)
    }

    //endregion

    //region Tracking GPS, Rotation, location availability, pin mode, click ...

    private fun onClickPin(properties: Map<String, String>) {
        when (pinMode) {
            PinMode.QUESTS -> {
                questPinsManager?.getQuestKey(properties)?.let { listener?.onClickedQuest(it) }
            }
            PinMode.EDITS -> {
                editHistoryPinsManager?.getEditKey(properties)?.let { listener?.onClickedEdit(it) }
            }
            PinMode.NONE -> {}
        }
    }

    private fun onClickElement(key: ElementKey) {
        listener?.onClickedElement(key)
    }

    private fun onClickMap(position: LatLng): Boolean {
        val fingerRadius = context?.resources?.dpToPx(CLICK_AREA_SIZE_IN_DP / 2) ?: return false
        val clickPos = map?.projection?.toScreenLocation(position) ?: return false

        // no feature: just click the map
        val fingerEdgePosition = map?.projection?.fromScreenLocation(PointF(clickPos.x + fingerRadius, clickPos.y)) ?: return false
        val fingerRadiusInMeters = position.distanceTo(fingerEdgePosition)
        listener?.onClickedMapAt(position.toLatLon(), fingerRadiusInMeters)
        return true
    }

    @SuppressLint("MissingPermission")
    private fun onLocationAvailabilityChanged(isAvailable: Boolean) {
        if (!isAvailable) {
            displayedLocation = null
            locationMapComponent?.targetLocation = null
        } else {
            locationManager.getCurrentLocation()
        }
    }

    private fun onCompassRotationChanged(rot: Float, tilt: Float) {
        locationMapComponent?.rotation = (rot * 180 / PI) - (map?.camera?.rotation ?: 0.0)
    }

    private fun onLocationChanged(location: Location) {
        displayedLocation = location
        recentLocationStore.add(location)
        locationMapComponent?.targetLocation = location
        addTrackLocation(location)
        compass.setLocation(location)
        centerCurrentPositionIfFollowing()
        listener?.onDisplayedLocationDidChange()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
        styleableOverlayManager?.onNewScreenPosition()
    }

    private fun onUpdatedPinMode() {
        /* both managers use the same resource (PinsMapComponent), so the newly visible manager
           may only be activated after the old has been deactivated
         */
        when (pinMode) {
            PinMode.QUESTS -> {
                editHistoryPinsManager?.isVisible = false
                questPinsManager?.isVisible = true
            }
            PinMode.EDITS -> {
                questPinsManager?.isVisible = false
                editHistoryPinsManager?.isVisible = true
            }
            else -> {
                questPinsManager?.isVisible = false
                editHistoryPinsManager?.isVisible = false
            }
        }
    }

    private fun onSelectedOverlayChanged() {
        val new = selectedOverlaySource.selectedOverlay?.hidesLayers.orEmpty()
        val old = previouslyHiddenLayers
        if (old == new) return

        viewLifecycleScope.launch {
            old.forEach { layer ->
                map?.style?.getLayer(layer)?.setProperties(visibility(Property.VISIBLE))
            }
            new.forEach { layer ->
                map?.style?.getLayer(layer)?.setProperties(visibility(Property.NONE))
            }
        }

        previouslyHiddenLayers = new
    }

    private fun addTrackLocation(location: Location) {
        // ignore if too imprecise
        if (location.accuracy > MIN_TRACK_ACCURACY) return
        val lastLocation = tracks.last().lastOrNull()

        // create new track if last position too old
        if (lastLocation != null && !isRecordingTracks) {
            if ((displayedLocation?.time ?: 0) - lastLocation.time > MAX_TIME_BETWEEN_LOCATIONS) {
                tracks.add(ArrayList())
                tracksMapComponent?.startNewTrack(false)
            }
        }
        val trackpoint = Trackpoint(location.toLatLon(), location.time, location.accuracy, location.altitude.toFloat())

        tracks.last().add(trackpoint)
        // in rare cases, onLocationChanged may already be called before the view has been created
        // so we need to check that first
        if (view != null) {
            tracksMapComponent?.addToCurrentTrack(trackpoint.position)
        }
    }

    //endregion

    //region Control focusing on and highlighting edit / quest / element

    /** Focus the view on the given geometry */
    fun startFocus(geometry: ElementGeometry, insets: Insets) {
        geometryMapComponent?.beginFocusGeometry(geometry, insets)
    }

    /** End the focussing but do not return to position before focussing */
    fun clearFocus() {
        geometryMapComponent?.clearFocusGeometry()
    }

    /** return to the position before focussing */
    fun endFocus() {
        geometryMapComponent?.endFocusGeometry()
    }

    fun highlightPins(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        viewLifecycleScope.launch(Dispatchers.Default) {
            selectedPinsMapComponent?.set(iconResId, pinPositions)
        }
    }

    fun hideNonHighlightedPins(questKey: QuestKey? = null) {
        pinsMapComponent?.setVisible(false)
    }

    fun hideOverlay() {
        styleableOverlayMapComponent?.setVisible(false)
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
    }

    /** Clear all highlighting */
    fun clearHighlighting() {
        pinsMapComponent?.setVisible(true)
        styleableOverlayMapComponent?.setVisible(true)
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
        selectedPinsMapComponent?.clear()
    }

    fun clearSelectedPins() {
        selectedPinsMapComponent?.clear()
    }

    override fun putMarkersForCurrentHighlighting(markers: Iterable<Marker>) {
        viewLifecycleScope.launch(Dispatchers.Default) {
            geometryMarkersMapComponent?.putAll(markers)
        }
    }

    @UiThread override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        geometryMarkersMapComponent?.delete(geometry)
    }

    @UiThread override fun clearMarkersForCurrentHighlighting() {
        geometryMarkersMapComponent?.clear()
    }

    //endregion

    //region Control position tracking

    @SuppressLint("MissingPermission")
    fun startPositionTracking() {
        locationMapComponent?.isVisible = true
        locationManager.requestUpdates(0, 5000, 1f)
    }

    fun stopPositionTracking() {
        locationMapComponent?.isVisible = false
        locationManager.removeUpdates()
    }

    fun clearPositionTracking() {
        stopPositionTracking()
        displayedLocation = null
        isNavigationMode = false

        tracks = ArrayList()
        tracks.add(ArrayList())
        tracksMapComponent?.clear()
    }

    fun startPositionTrackRecording() {
        isRecordingTracks = true
        _recordedTracks.clear()
        tracks.add(ArrayList())
        locationMapComponent?.isVisible = true
        tracksMapComponent?.startNewTrack(true)
    }

    fun stopPositionTrackRecording() {
        isRecordingTracks = false
        _recordedTracks.clear()
        _recordedTracks.addAll(tracks.last())
        tracks.add(ArrayList())
        tracksMapComponent?.startNewTrack(false)
    }

    private fun centerCurrentPosition() {
        val displayedPosition = displayedLocation?.toLatLon() ?: return

        updateCameraPosition(600) {
            if (isNavigationMode) {
                val bearing = getTrackBearing(tracks.last())
                if (bearing != null) rotation = bearing
                tilt = 60.0
            }

            position = displayedPosition

            if (!zoomedYet) {
                zoomedYet = true
                val currentZoom = map?.camera?.zoom
                if (currentZoom != null && currentZoom < 17.0f) zoom = 18.0
            }
        }
    }

    fun centerCurrentPositionIfFollowing() {
        if (isFollowingPosition) centerCurrentPosition()
    }

    private fun onUpdatedNavigationMode() {
        if (!isNavigationMode) {
            updateCameraPosition(300) {
                rotation = 0.0
                tilt = 0.0
            }
        } else {
            centerCurrentPositionIfFollowing()
        }
    }

    //endregion

    //region Save and restore state

    private fun restoreMapState() {
        isFollowingPosition = prefs.mapIsFollowing
        isNavigationMode = prefs.mapIsNavigationMode
    }

    private fun saveMapState() {
        prefs.mapIsFollowing = isFollowingPosition
        prefs.mapIsNavigationMode = isNavigationMode
    }

    //endregion

    companion object {
        private const val DISPLAYED_LOCATION = "displayed_location"
        private const val TRACKS = "tracks"
        private const val TRACKS_IS_RECORDING = "tracks_is_recording"

        private const val MIN_TRACK_ACCURACY = 20f
        private const val MAX_TIME_BETWEEN_LOCATIONS = 60L * 1000 // 1 minute

        private const val CLICK_AREA_SIZE_IN_DP = 24
    }
}

private fun <T> ArrayList<ArrayList<T>>.takeLastNested(n: Int): ArrayList<ArrayList<T>> {
    var sum = 0
    for (i in lastIndex downTo 0) {
        val s = get(i).size
        if (sum + s > n) {
            val result = ArrayList(subList(i + 1, size))
            if (n > sum) result.add(0, ArrayList(get(i).takeLast(n - sum)))
            return result
        }
        sum += s
    }
    return this
}
