package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.serialization.Serializable
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MapState
import kotlin.math.abs
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

    /** A pan only stops following once a location is displayed, as in the legacy map. */
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
            map.animateCameraPosition(map.cameraPosition.copy(tilt = 0.0), 300.milliseconds)
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
        ), 600.milliseconds)
    }

    suspend fun resetCompass() {
        settings = settings.copy(navigating = false)
        map.animateCameraPosition(map.cameraPosition.copy(bearing = 0.0, tilt = 0.0), 300.milliseconds)
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

    suspend fun focus(geometry: ElementGeometry) {
        val camera = map.cameraPosition
        if (settings.previousFocus == null) {
            settings = settings.copy(previousFocus = FocusCamera(camera.target.toLatLon(), camera.zoom))
        }
        if (geometry is ElementPointGeometry) {
            val difference = abs(camera.zoom - 19.0)
            map.animateCameraPosition(camera.copy(
                target = geometry.center.toPosition(),
                zoom = if (difference > 0.5) 19.0 else camera.zoom,
            ), maxOf(450, (difference * 450).roundToInt()).milliseconds)
        } else {
            // TODO maplibre-compose: Query the fitted camera to restore the 0.75 zoom margin,
            // maximum zoom 19, 0.5 zoom threshold, and zoom-dependent duration (as for clusters).
            map.animateCameraToBounds(
                geometry.bounds.toGeoJsonBoundingBox(), camera.bearing, camera.tilt,
                duration = 450.milliseconds,
            )
        }
    }

    fun clearFocus() {
        settings = settings.copy(previousFocus = null)
    }

    suspend fun endFocus() {
        val previous = settings.previousFocus ?: return
        clearFocus()
        val camera = map.cameraPosition
        // Restore the pre-focus target and zoom, keeping the user's current bearing and tilt.
        map.animateCameraPosition(camera.copy(target = previous.position.toPosition(), zoom = previous.zoom),
            maxOf(300, (abs(camera.zoom - previous.zoom) * 300).roundToInt()).milliseconds)
    }
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
