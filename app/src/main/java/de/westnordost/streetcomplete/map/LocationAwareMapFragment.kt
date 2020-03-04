package de.westnordost.streetcomplete.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.hardware.SensorManager
import android.location.Location
import android.view.animation.DecelerateInterpolator
import android.location.LocationManager
import android.view.WindowManager
import androidx.core.content.edit
import com.mapzen.tangram.*
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.location.FineLocationManager
import de.westnordost.streetcomplete.util.BitmapUtil
import de.westnordost.streetcomplete.map.tangram.Marker
import de.westnordost.streetcomplete.util.EARTH_CIRCUMFERENCE
import kotlin.math.*

/** Manages a map that shows the device's GPS location and orientation as markers on the map with
 *  the option to let the screen follow the location and rotation */
open class LocationAwareMapFragment : MapFragment() {

    private lateinit var compass: Compass
    private lateinit var locationManager: FineLocationManager

    // markers showing the user's location, direction and accuracy of location
    private var locationMarker: Marker? = null
    private var accuracyMarker: Marker? = null
    private var directionMarker: Marker? = null

    /** The location of the GPS location dot on the map. Null if none (yet) */
    var displayedLocation: Location? = null
        private set

    /** Whether the view rotation is displayed on the map. False if the device orientation is unknown (yet) */
    var isShowingDirection = false
        private set

    private var directionMarkerSize: PointF? = null

    private val displayedPosition: LatLon? get() = displayedLocation?.let { OsmLatLon(it.latitude, it.longitude) }

    /** Whether the view should automatically center on the GPS location */
    var isFollowingPosition = false
        set(value) {
            field = value
            followPosition()
        }

    /** When the view follows the GPS position, whether the view already zoomed to the location once*/
    private var zoomedYet = false

    /** Whether the view should automatically rotate with the compass (like during navigation) */
    // Since the with-compass rotation happens with no animation, it's better to start the tilt
    // animation abruptly and slide out, rather than sliding in and out (the default interpolator)
    private val interpolator = DecelerateInterpolator()
    var isCompassMode: Boolean = false
        set(value) {
            field = value
            if (value) {
                controller?.updateCameraPosition(300, interpolator) { tilt = PI.toFloat() / 5f }
            }
        }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        compass = Compass(
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager,
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay,
            this::onCompassRotationChanged
        )
        locationManager = FineLocationManager(
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager,
            this::onLocationChanged
        )
    }

    override fun onResume() {
        super.onResume()
        compass.onResume()
    }

    override fun onPause() {
        super.onPause()
        compass.onPause()
        saveMapState()
    }

    override fun onStop() {
        super.onStop()
        stopPositionTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        compass.onDestroy()
        controller = null
        locationMarker = null
        directionMarker = null
        accuracyMarker = null
    }

    /* ---------------------------------- Map State Callbacks ----------------------------------- */

    override fun onMapReady() {
        super.onMapReady()
        restoreMapState()
        initMarkers()
        followPosition()
        showLocation()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        updateAccuracy()
    }

    /* ---------------------------------------- Markers ----------------------------------------- */

    private fun initMarkers() {
        locationMarker = createLocationMarker()
        directionMarker = createDirectionMarker()
        accuracyMarker = createAccuracyMarker()
    }

    private fun createLocationMarker(): Marker? {
        val ctx = context ?: return null

        val dot = BitmapUtil.createBitmapDrawableFrom(ctx.resources, R.drawable.location_dot)
        val dotWidth = dot.intrinsicWidth.toFloat().toDp(ctx)
        val dotHeight = dot.intrinsicHeight.toFloat().toDp(ctx)

        val marker = controller?.addMarker() ?: return null
        marker.setStylingFromString(
            "{ style: 'points', color: 'white', size: [${dotWidth}px, ${dotHeight}px], order: 2000, flat: true, collide: false }"
        )
        marker.setDrawable(dot)
        marker.setDrawOrder(9)
        return marker
    }

    private fun createDirectionMarker(): Marker? {
        val ctx = context ?: return null

        val directionImg = BitmapUtil.createBitmapDrawableFrom(ctx.resources, R.drawable.location_direction)
        directionMarkerSize = PointF(
            directionImg.intrinsicWidth.toFloat().toDp(ctx),
            directionImg.intrinsicHeight.toFloat().toDp(ctx)
        )

        val marker = controller?.addMarker() ?: return null
        marker.setDrawable(directionImg)
        marker.setDrawOrder(2)
        return marker
    }

    private fun createAccuracyMarker(): Marker? {
        val ctx = context ?: return null
        val accuracyImg = BitmapUtil.createBitmapDrawableFrom(ctx.resources, R.drawable.accuracy_circle)

        val marker = controller?.addMarker() ?: return null
        marker.setDrawable(accuracyImg)
        marker.setDrawOrder(1)
        return marker
    }

    private fun showLocation() {
        val pos = displayedPosition ?: return

        accuracyMarker?.isVisible = true
        accuracyMarker?.setPointEased(pos, 1000, MapController.EaseType.CUBIC)
        locationMarker?.isVisible = true
        locationMarker?.setPointEased(pos, 1000, MapController.EaseType.CUBIC)
        directionMarker?.isVisible = isShowingDirection
        directionMarker?.setPointEased(pos, 1000, MapController.EaseType.CUBIC)

        updateAccuracy()
    }

    private fun updateAccuracy() {
        val location = displayedLocation ?: return
        if (accuracyMarker?.isVisible != true) return

        val zoom = controller!!.cameraPosition.zoom
        val size = location.accuracy * pixelsPerMeter(location.latitude, zoom)
        accuracyMarker?.setStylingFromString(
            "{ style: 'points', color: 'white', size: ${size}px, order: 2000, flat: true, collide: false }"
        )
    }

    private fun pixelsPerMeter(latitude: Double, zoom: Float): Double {
        val numberOfTiles = (2.0).pow(zoom.toDouble())
        val metersPerTile = cos(latitude * PI / 180.0) * EARTH_CIRCUMFERENCE / numberOfTiles
        return 256 / metersPerTile
    }

    /* --------------------------------- Position tracking -------------------------------------- */

    @SuppressLint("MissingPermission")
    fun startPositionTracking() {
        zoomedYet = false
        displayedLocation = null
        locationManager.requestUpdates(2000, 5f)
    }

    fun stopPositionTracking() {
        locationMarker?.isVisible = false
        accuracyMarker?.isVisible = false
        directionMarker?.isVisible = false

        displayedLocation = null
        zoomedYet = false
        isShowingDirection = false

        locationManager.removeUpdates()
    }

    protected open fun shouldCenterCurrentPosition(): Boolean {
        return isFollowingPosition
    }

    protected fun followPosition() {
        if (!shouldCenterCurrentPosition()) return
        val pos = displayedPosition ?: return

        controller?.updateCameraPosition(500) {
            position = pos
            if (!zoomedYet) {
                zoomedYet = true
                zoom = 19f
            }
        }
    }

    private fun onLocationChanged(location: Location) {
        displayedLocation = location
        compass.setLocation(location)
        showLocation()
        followPosition()
    }

    /* --------------------------------- Rotation tracking -------------------------------------- */

    private fun onCompassRotationChanged(rot: Float, tilt: Float) {
        // we received an event from the compass, so compass is working - direction can be displayed on screen
        isShowingDirection = true
        directionMarker?.let {
            if (!it.isVisible) it.isVisible = true
            val angle = rot * 180 / PI
            val size = directionMarkerSize
            if (size != null) {
                it.setStylingFromString(
                    "{ style: 'points', color: '#cc536dfe', size: [${size.x}px, ${size.y}px], order: 2000, collide: false, flat: true, angle: $angle}"
                )
            }
        }

        if (isCompassMode) {
            controller?.updateCameraPosition { rotation = -rot }
        }
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
