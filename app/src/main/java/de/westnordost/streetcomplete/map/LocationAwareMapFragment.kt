package de.westnordost.streetcomplete.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.content.edit
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.location.FineLocationManager
import de.westnordost.streetcomplete.map.components.CurrentLocationMapComponent
import kotlin.math.PI

/** Manages a map that shows the device's GPS location and orientation as markers on the map with
 *  the option to let the screen follow the location and rotation */
open class LocationAwareMapFragment : MapFragment() {

    private lateinit var compass: Compass
    private lateinit var locationManager: FineLocationManager

    private var locationMapComponent: CurrentLocationMapComponent? = null

    var displayedLocation: Location? = null
    private var compassRotation: Double? = null

    /** Whether the view should automatically center on the GPS location */
    var isFollowingPosition = false
        set(value) {
            field = value
            centerCurrentPositionIfFollowing()
        }

    /** When the view follows the GPS position, whether the view already zoomed to the location once*/
    private var zoomedYet = false

    /** Whether the view should automatically rotate with the compass (like during navigation) */
    // Since the with-compass rotation happens with no animation, it's better to start the tilt
    // animation abruptly and slide out, rather than sliding in and out (the default interpolator)
    private val interpolator = DecelerateInterpolator()
    var isCompassMode: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                controller?.updateCameraPosition(300, interpolator) {
                    tilt = if (value) PI.toFloat() / 5f else 0f
                }
            }
        }
    private var viewDirection: Float? = null

    interface Listener {
        /** Called after the map fragment updated its displayed location */
        fun onLocationDidChange()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener


    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        compass = Compass(context.getSystemService<SensorManager>()!!,
            context.getSystemService<WindowManager>()!!.defaultDisplay,
            this::onCompassRotationChanged
        )
        lifecycle.addObserver(compass)
        locationManager = FineLocationManager(
            context.getSystemService<LocationManager>()!!,
            this::onLocationChanged
        )
    }

    override fun onStop() {
        super.onStop()
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
        locationManager.requestUpdates(2000, 5f)
    }

    fun stopPositionTracking() {
        locationMapComponent?.isVisible = false
        locationManager.removeUpdates()
    }

    protected open fun shouldCenterCurrentPosition(): Boolean {
        return isFollowingPosition
    }

    fun centerCurrentPosition() {
        val controller = controller ?: return
        val targetPosition = displayedLocation?.let { LatLon(it.latitude, it.longitude) } ?: return
        controller.updateCameraPosition(600) {
            position = targetPosition
            if (!zoomedYet) {
                zoomedYet = true
                zoom = 19f
            }
        }
    }

    protected fun centerCurrentPositionIfFollowing() {
        if (shouldCenterCurrentPosition()) centerCurrentPosition()
    }

    private fun onLocationChanged(location: Location) {
        this.displayedLocation = location
        locationMapComponent?.location = location
        compass.setLocation(location)
        centerCurrentPositionIfFollowing()
        listener?.onLocationDidChange()
    }

    /* --------------------------------- Rotation tracking -------------------------------------- */

    private fun onCompassRotationChanged(rot: Float, tilt: Float) {
        compassRotation = rot * 180 / PI
        locationMapComponent?.rotation = compassRotation

        if (isCompassMode) {
            viewDirection =
                if (viewDirection == null) -rot
                else smoothenAngle(-rot, viewDirection ?: 0f, 0.05f)

            controller?.updateCameraPosition { rotation = viewDirection }
        }
    }

    private fun smoothenAngle( newValue: Float, oldValue: Float, factor: Float): Float {
        var delta = newValue - oldValue
        while (delta > +PI) delta -= 2 * PI.toFloat()
        while (delta < -PI) delta += 2 * PI.toFloat()
        return oldValue + factor * delta
    }

    /* -------------------------------- Save and Restore State ---------------------------------- */

    private fun restoreMapState() {
        val prefs = activity?.getPreferences(Activity.MODE_PRIVATE) ?: return
        isFollowingPosition = prefs.getBoolean(PREF_FOLLOWING, true)
        isCompassMode = prefs.getBoolean(PREF_COMPASS_MODE, false)
    }

    private fun saveMapState() {
        activity?.getPreferences(Activity.MODE_PRIVATE)?.edit {
            putBoolean(PREF_FOLLOWING, isFollowingPosition)
            putBoolean(PREF_COMPASS_MODE, isCompassMode)
        }
    }

    companion object {
        const val PREF_FOLLOWING = "map_following"
        const val PREF_COMPASS_MODE = "map_compass_mode"
    }
}
