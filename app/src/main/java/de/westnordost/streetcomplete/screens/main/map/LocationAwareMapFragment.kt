package de.westnordost.streetcomplete.screens.main.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.edit
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.screens.main.map.components.CurrentLocationMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.TracksMapComponent
import de.westnordost.streetcomplete.screens.main.map.tangram.screenBottomToCenterDistance
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.PI

/** Manages a map that shows the device's GPS location and orientation as markers on the map with
 *  the option to let the screen follow the location and rotation */
open class LocationAwareMapFragment : MapFragment() {

    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()

    private lateinit var compass: Compass
    private lateinit var locationManager: FineLocationManager

    private var locationMapComponent: CurrentLocationMapComponent? = null
    private var tracksMapComponent: TracksMapComponent? = null

    /** The GPS position at which the user is displayed at */
    var displayedLocation: Location? = null
        private set

    /** The GPS trackpoints the user has walked */
    private var tracks: MutableList<ArrayList<Location>>

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
                updateCameraPosition(300) { tilt = 0f }
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
            context.getSystemService<WindowManager>()!!.defaultDisplay,
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
            val nullTerminatedTracks = savedInstanceState.getParcelableArrayList<Location?>(TRACKS) as ArrayList<Location?>?
            if (nullTerminatedTracks != null) {
                tracks = nullTerminatedTracks.unflattenNullTerminated()
                // unflattenNullTerminated creates an empty list item (i.e. a new track) at the end.
                // This is fine if the track is not being recorded.
                if (isRecordingTracks) {
                    tracks.removeLastOrNull()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(requireContext().run { hasLocationPermission && isLocationEnabled })
    }

    override fun onStop() {
        super.onStop()
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)
        saveMapState()
        stopPositionTracking()
    }

    /* ---------------------------------- Map State Callbacks ----------------------------------- */

    override suspend fun onMapReady() {
        super.onMapReady()
        restoreMapState()

        val ctrl = controller ?: return
        val ctx = context ?: return
        locationMapComponent = CurrentLocationMapComponent(ctx, ctrl)
        locationMapComponent?.location = displayedLocation

        tracksMapComponent = TracksMapComponent(ctrl)
        tracksMapComponent?.setTracks(tracks, isRecordingTracks)

        centerCurrentPositionIfFollowing()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        locationMapComponent?.currentMapZoom = zoom
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

    @SuppressLint("MissingPermission")
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
        tracks.last().forEach {
            _recordedTracks.add(
                Trackpoint(
                    LatLon(it.latitude, it.longitude),
                    it.time, // in milliseconds
                    it.accuracy,
                    it.altitude.toFloat() // always zero in emulator: https://stackoverflow.com/q/65325665
                )
            )
        }
        tracks.add(ArrayList())
        tracksMapComponent?.startNewTrack(false)
    }

    protected open fun shouldCenterCurrentPosition(): Boolean {
        return isFollowingPosition
    }

    fun centerCurrentPosition() {
        val displayedPosition = displayedLocation?.toLatLon() ?: return
        var centerPosition = displayedPosition

        updateCameraPosition(600) {
            if (isNavigationMode) {
                val bearing = getTrackBearing(tracks.last())
                if (bearing != null) {
                    rotation = -(bearing * PI / 180.0).toFloat()
                    /* move center position down a bit, so there is more space in front of than
                       behind user */
                    val distance = controller?.screenBottomToCenterDistance()
                    if (distance != null) {
                        centerPosition = centerPosition.translate(distance * 0.4, bearing.toDouble())
                    }
                }
                tilt = PI.toFloat() / 6f
            }

            position = centerPosition

            if (!zoomedYet) {
                zoomedYet = true
                zoom = 19f
            }
        }
    }

    fun centerCurrentPositionIfFollowing() {
        if (shouldCenterCurrentPosition()) centerCurrentPosition()
    }

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (!isAvailable) {
            displayedLocation = null
            locationMapComponent?.location = null
        }
    }

    private fun onLocationChanged(location: Location) {
        displayedLocation = location
        locationMapComponent?.location = location
        addTrackLocation(location)
        compass.setLocation(location)
        centerCurrentPositionIfFollowing()
        listener?.onDisplayedLocationDidChange()
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

        tracks.last().add(location)
        // delay update by 600 ms because the animation to the new location takes that long
        // in rare cases, onLocationChanged may already be called before the view has been created
        // so we need to check that first
        if (view != null) {
            viewLifecycleScope.launch {
                delay(600)
                tracksMapComponent?.addToCurrentTrack(location)
            }
        }
    }

    /* --------------------------------- Rotation tracking -------------------------------------- */

    private fun onCompassRotationChanged(rot: Float, tilt: Float) {
        locationMapComponent?.rotation = rot * 180 / PI
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val prefs = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        isFollowingPosition = prefs.getBoolean(PREF_FOLLOWING, true)
        isNavigationMode = prefs.getBoolean(PREF_NAVIGATION_MODE, false)
    }

    private fun saveMapState() {
        activity?.getPreferences(Activity.MODE_PRIVATE)?.edit {
            putBoolean(PREF_FOLLOWING, isFollowingPosition)
            putBoolean(PREF_NAVIGATION_MODE, isNavigationMode)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(DISPLAYED_LOCATION, displayedLocation)
        outState.putParcelableArrayList(TRACKS, tracks.flattenToNullTerminated())
        outState.putBoolean(TRACKS_IS_RECORDING, isRecordingTracks)
    }

    companion object {
        private const val PREF_FOLLOWING = "map_following"
        private const val PREF_NAVIGATION_MODE = "map_compass_mode"

        private const val DISPLAYED_LOCATION = "displayed_location"
        private const val TRACKS = "tracks"
        private const val TRACKS_IS_RECORDING = "tracks_is_recording"

        private const val MIN_TRACK_ACCURACY = 20f
        private const val MAX_TIME_BETWEEN_LOCATIONS = 60L * 1000 // 1 minute
    }
}

private fun <T> List<List<T>>.flattenToNullTerminated(): ArrayList<T?> =
    ArrayList(flatMap { it + null })

private fun <T> List<T?>.unflattenNullTerminated(): ArrayList<ArrayList<T>> {
    val result = ArrayList<ArrayList<T>>()
    var current = ArrayList<T>()
    for (it in this) {
        if (it != null) {
            current.add(it)
        } else {
            result.add(current)
            current = ArrayList()
        }
    }
    result.add(current)
    return result
}
