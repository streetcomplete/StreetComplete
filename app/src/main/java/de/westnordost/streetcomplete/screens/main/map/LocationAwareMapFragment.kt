package de.westnordost.streetcomplete.screens.main.map

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.getSystemService
import org.maplibre.android.maps.MapLibreMap
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.location.RecentLocationStore
import de.westnordost.streetcomplete.data.map.MapStateStore
import org.maplibre.android.maps.Style
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.screens.main.map.components.CurrentLocationMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.TracksMapComponent
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import kotlin.math.PI

/** Manages a map that shows the device's GPS location and orientation as markers on the map with
 *  the option to let the screen follow the location and rotation */
open class LocationAwareMapFragment : MapFragment() {

    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val recentLocationStore: RecentLocationStore by inject()
    private val mapStateStore: MapStateStore by inject()

    private lateinit var compass: Compass
    private lateinit var locationManager: FineLocationManager

    private var locationMapComponent: CurrentLocationMapComponent? = null
    private var tracksMapComponent: TracksMapComponent? = null

    /** The GPS position at which the user is displayed at */
    var displayedLocation: Location? = null
        private set

    /** The GPS trackpoints the user has walked */
    private var tracks: ArrayList<ArrayList<Trackpoint>>

    /** If we are actively recording track history */
    var isRecordingTracks = false
        private set

    /** The GPS trackpoints the user has recorded */
    private var _recordedTracks: ArrayList<Trackpoint>

    val recordedTracks: List<Trackpoint> get() = _recordedTracks

    /** Whether the view should automatically center on the GPS location */
    var isFollowingPosition = true
        set(value) {
            field = value
            if (field != value && !value) {
                _isNavigationMode = false
            }
        }

    /** Whether the view should automatically rotate with bearing (like during navigation) */
    private var _isNavigationMode: Boolean = false
    var isNavigationMode: Boolean
        set(value) {
            if (_isNavigationMode != value && !value) {
                updateCameraPosition(300) { tilt = 0.0 }
            }
            _isNavigationMode = value
        }
        get() = _isNavigationMode

    /** When the view follows the GPS position, whether the view already zoomed to the location once*/
    private var zoomedYet = false

    interface Listener {
        /** Called after the map fragment updated its displayed location */
        fun onDisplayedLocationDidChange()
    }

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        tracks = ArrayList()
        tracks.add(ArrayList())
        _recordedTracks = ArrayList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        compass = Compass(
            context.getSystemService<SensorManager>()!!,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display!!
            } else {
                context.getSystemService<WindowManager>()!!.defaultDisplay
            },
            this::onCompassRotationChanged
        )
        lifecycle.addObserver(compass)
        locationManager = FineLocationManager(context, this::onLocationChanged)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore value of members from saved state
        if (savedInstanceState != null) {
            displayedLocation = savedInstanceState.getParcelable(DISPLAYED_LOCATION)
            isRecordingTracks = savedInstanceState.getBoolean(TRACKS_IS_RECORDING)
            tracks = Json.decodeFromString(savedInstanceState.getString(TRACKS)!!)
        }
    }

    override fun onStart() {
        super.onStart()
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(requireContext().isLocationAvailable)
    }

    override fun onStop() {
        super.onStop()
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)
        saveMapState()
        stopPositionTracking()
    }

    /* ---------------------------------- Map State Callbacks ----------------------------------- */

    override suspend fun onMapReady(map: MapLibreMap, style: Style) {
        super.onMapReady(map, style)
        restoreMapState()

        val ctx = context ?: return
        locationMapComponent = CurrentLocationMapComponent(ctx, style, map).also { locationMapComponent ->
            viewLifecycleOwner.lifecycle.addObserver(locationMapComponent)
        }

        tracksMapComponent = TracksMapComponent(ctx, style, map).also { tracksMapComponent ->
            viewLifecycleOwner.lifecycle.addObserver(tracksMapComponent)
            val positionsLists = tracks.map { track -> track.map { it.position } }
            tracksMapComponent.setTracks(positionsLists, isRecordingTracks)
        }

        tracksMapComponent?.layers?.forEach { map.style?.addLayer(it) }
        locationMapComponent?.layers?.forEach { map.style?.addLayer(it) }

        locationMapComponent?.targetLocation = displayedLocation

        centerCurrentPositionIfFollowing()
    }

    /* --------------------------------- Position tracking -------------------------------------- */

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

    protected open fun shouldCenterCurrentPosition(): Boolean {
        return isFollowingPosition
    }

    fun centerCurrentPosition() {
        val displayedPosition = displayedLocation?.toLatLon() ?: return

        updateCameraPosition(600) {
            if (isNavigationMode) {
                val bearing = getTrackBearing(tracks.last())
                if (bearing != null) {
                    rotation = -(bearing * PI / 180.0)
                }
                tilt = 60.0 // looks like we use degrees
            }

            position = displayedPosition

            if (!zoomedYet) {
                zoomedYet = true
                zoom = 19.0
            }
        }
    }

    fun centerCurrentPositionIfFollowing() {
        if (shouldCenterCurrentPosition()) centerCurrentPosition()
    }

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (!isAvailable) {
            displayedLocation = null
            locationMapComponent?.targetLocation = null
        }
    }

    private fun onLocationChanged(location: Location) {
        viewLifecycleScope.launch {
            displayedLocation = location
            recentLocationStore.add(location)
            locationMapComponent?.targetLocation = location
            addTrackLocation(location)
            compass.setLocation(location)
            centerCurrentPositionIfFollowing()
            listener?.onDisplayedLocationDidChange()
        }
    }

    private suspend fun addTrackLocation(location: Location) {
        // ignore if too imprecise
        if (location.accuracy > MIN_TRACK_ACCURACY) return
        val lastLocation = tracks.last().lastOrNull()

        // create new track if last position too old
        if (lastLocation != null && !isRecordingTracks) {
            if ((displayedLocation?.time ?: 0) - lastLocation.time > MAX_TIME_BETWEEN_LOCATIONS) {
                tracks.add(ArrayList())
                withContext(Dispatchers.Main) { tracksMapComponent?.startNewTrack(false) }
            }
        }
        val trackpoint = Trackpoint(location.toLatLon(), location.time, location.accuracy, location.altitude.toFloat())

        tracks.last().add(trackpoint)
        // in rare cases, onLocationChanged may already be called before the view has been created
        // so we need to check that first
        if (view != null) {
            withContext(Dispatchers.Main) { tracksMapComponent?.addToCurrentTrack(trackpoint.position) }
        }
    }

    /* --------------------------------- Rotation tracking -------------------------------------- */

    private fun onCompassRotationChanged(rot: Float, tilt: Float) {
        locationMapComponent?.rotation = rot * 180 / PI
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        isFollowingPosition = mapStateStore.isFollowingPosition
        isNavigationMode = mapStateStore.isNavigationMode
    }

    private fun saveMapState() {
        mapStateStore.isFollowingPosition = isFollowingPosition
        mapStateStore.isNavigationMode = isNavigationMode
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(DISPLAYED_LOCATION, displayedLocation)
        /* tracks can theoretically grow indefinitely, but one cannot put an indefinite amount of
         * data into the saved instance state. Apparently only half an MB (~5000 trackpoints). So
         * let's cut off at 1000 points to be on the safe side... (I don't know if the limit is
         * for all of the app or just one fragment) */
        outState.putString(TRACKS, Json.encodeToString(tracks.takeLastNested(1000)))
        outState.putBoolean(TRACKS_IS_RECORDING, isRecordingTracks)
    }

    companion object {
        private const val DISPLAYED_LOCATION = "displayed_location"
        private const val TRACKS = "tracks"
        private const val TRACKS_IS_RECORDING = "tracks_is_recording"

        private const val MIN_TRACK_ACCURACY = 20f
        private const val MAX_TIME_BETWEEN_LOCATIONS = 60L * 1000 // 1 minute
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
