package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.serialization.Serializable
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MapState
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberMainMapCameraState(
    mapState: MapState,
    initiallyFollowing: Boolean = true,
    initiallyNavigating: Boolean = false,
): MainMapCameraState {
    val settings = rememberSerializable {
        mutableStateOf(MapCameraSettings(initiallyFollowing, initiallyNavigating))
    }
    return remember(mapState) { MainMapCameraState(mapState, settings) }
}

/** StreetComplete's camera policy; the map itself owns its camera position and animations. */
class MainMapCameraState internal constructor(
    private val map: MapState,
    settings: MutableState<MapCameraSettings>,
) {
    private var settings by settings

    var isFollowingPosition: Boolean
        get() = settings.following
        set(value) { settings = settings.copy(following = value, zoomedYet = value && settings.zoomedYet) }

    val isNavigationMode: Boolean get() = settings.navigating

    /** Panning only stops following once a location is displayed; the first fix still centers
     *  the map after panning while waiting for it. */
    fun onPan(hasLocation: Boolean) {
        if (hasLocation) isFollowingPosition = false
    }

    suspend fun setNavigationMode(value: Boolean, location: LatLon?, bearing: Double?) {
        if (settings.navigating == value) return
        settings = settings.copy(navigating = value)
        if (value) {
            followLocation(location, bearing)
        } else {
            // Leaving navigation retains the bearing; the compass resets it explicitly.
            map.animateCameraPosition(map.cameraPosition.copy(tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
        }
    }

    suspend fun followLocation(location: LatLon?, bearing: Double?) {
        if (!isFollowingPosition || location == null) return
        val camera = map.cameraPosition
        val zoom = if (!settings.zoomedYet && camera.zoom < 17.0) 18.0 else camera.zoom
        settings = settings.copy(zoomedYet = true)
        map.animateCameraPosition(camera.copy(
            target = location.toPosition(),
            zoom = zoom,
            bearing = if (isNavigationMode) bearing ?: camera.bearing else camera.bearing,
            tilt = if (isNavigationMode) 60.0 else camera.tilt,
        ), CameraAnimation.Ease(600.milliseconds))
    }

    suspend fun resetCompass() {
        settings = settings.copy(navigating = false)
        map.animateCameraPosition(map.cameraPosition.copy(bearing = 0.0, tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
    }

    suspend fun freeze() {
        if (settings.frozenFollowing == null) {
            settings = settings.copy(frozenFollowing = isFollowingPosition, frozenNavigation = isNavigationMode)
        }
        isFollowingPosition = false
        setNavigationMode(false, null, null)
    }

    suspend fun unfreeze(location: LatLon?, bearing: Double?) {
        val following = settings.frozenFollowing ?: return
        val navigating = settings.frozenNavigation
        settings = settings.copy(frozenFollowing = null, frozenNavigation = false)
        isFollowingPosition = following
        setNavigationMode(navigating, location, bearing)
    }

    fun moveTo(position: CameraPosition) {
        settings = settings.copy(following = false, navigating = false, zoomedYet = false)
        // MapState retains this update even before a map surface is attached.
        map.setCameraPosition(position)
    }

    /** Moves the camera to the [geometry]. If [restorable], [endFocus] moves it back afterwards. */
    suspend fun focus(geometry: ElementGeometry, restorable: Boolean) {
        val camera = map.cameraPosition
        if (restorable && settings.previousFocus == null) {
            settings = settings.copy(previousFocus = FocusCamera(camera.target.toLatLon(), camera.zoom))
        }
        val fitted = map.cameraForGeometry(geometry.toGeometry(), camera.bearing, camera.tilt)
        // zoom in a bit less than fully to keep a margin around the element, and not too far for points
        val targetZoom = min(fitted.zoom - 0.75, 19.0)
        val zoomDiff = abs(camera.zoom - targetZoom)
        map.animateCameraPosition(
            camera.copy(
                target = fitted.target,
                // only zoom if the difference is big enough
                zoom = if (zoomDiff > 0.5) targetZoom else camera.zoom,
            ),
            CameraAnimation.Ease(maxOf(450, (zoomDiff * 450).roundToInt()).milliseconds),
        )
    }

    suspend fun endFocus() {
        val previous = settings.previousFocus ?: return
        settings = settings.copy(previousFocus = null)
        val camera = map.cameraPosition
        // Restore the pre-focus target and zoom, keeping the user's current bearing and tilt.
        map.animateCameraPosition(
            camera.copy(target = previous.position.toPosition(), zoom = previous.zoom),
            CameraAnimation.Ease(maxOf(300, (abs(camera.zoom - previous.zoom) * 300).roundToInt()).milliseconds),
        )
    }
}

/** Zooms in on the pins of a clicked cluster */
suspend fun MapState.zoomToCluster(bounds: BoundingBox) {
    val camera = cameraPosition
    val fitted = cameraForBounds(bounds.toGeoJsonBoundingBox(), camera.bearing, camera.tilt)
    // zoom in a bit less than fully to show the pins completely, and not too far
    val targetZoom = min(fitted.zoom - 0.25, 19.0)
    val zoomDiff = abs(camera.zoom - targetZoom)
    animateCameraPosition(
        camera.copy(target = fitted.target, zoom = targetZoom),
        CameraAnimation.Ease(maxOf(450, (zoomDiff * 450).roundToInt()).milliseconds),
    )
}

@Serializable
internal data class MapCameraSettings(
    val following: Boolean,
    val navigating: Boolean,
    val zoomedYet: Boolean = false,
    val frozenFollowing: Boolean? = null,
    val frozenNavigation: Boolean = false,
    val previousFocus: FocusCamera? = null,
)

@Serializable
internal data class FocusCamera(val position: LatLon, val zoom: Double)
