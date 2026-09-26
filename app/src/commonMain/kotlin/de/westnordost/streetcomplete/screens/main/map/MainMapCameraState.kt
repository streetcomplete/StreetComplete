package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toPosition
import kotlinx.serialization.Serializable
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.MapState
import org.maplibre.compose.util.DpPadding
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberMainMapCameraState(
    mapState: MapState,
    initiallyFollowing: Boolean = true,
    initiallyNavigating: Boolean = false,
): MainMapCameraState {
    val isFollowingPosition = rememberSaveable { mutableStateOf(initiallyFollowing) }
    val isNavigationMode = rememberSaveable { mutableStateOf(initiallyNavigating) }
    val zoomedYet = rememberSaveable { mutableStateOf(false) }
    val mode = rememberSerializable { mutableStateOf<CameraMode>(CameraMode.Browsing) }
    return remember(mapState) {
        MainMapCameraState(mapState, mode, isFollowingPosition, isNavigationMode, zoomedYet)
    }
}

/** Owns camera transitions; MapLibre owns the position and the running animation. */
class MainMapCameraState internal constructor(
    private val map: MapState,
    mode: MutableState<CameraMode>,
    isFollowingPositionState: MutableState<Boolean>,
    isNavigationModeState: MutableState<Boolean>,
    zoomedYet: MutableState<Boolean>,
) {
    private var mode by mode

    var isFollowingPosition: Boolean by isFollowingPositionState
        private set

    var isNavigationMode: Boolean by isNavigationModeState
        private set

    private var zoomedYet by zoomedYet

    suspend fun zoomBy(amount: Double) {
        map.animateCamera(CameraUpdate(zoom = map.cameraPosition.zoom + amount), SnapAnimation)
    }

    /** Start following the current position.
     *  Special case when a sheet is open: Just zoom to the position, don't start to follow it. */
    suspend fun followPosition(position: LatLon?, bearing: Double?) {
        if (mode !is CameraMode.Browsing) {
            // An explicit location click can recenter an open form without resuming GPS following.
            if (position != null) {
                map.animateCamera(
                    update = CameraUpdate(
                        target = position.toPosition(),
                        zoom = if (map.cameraPosition.zoom < 17.0) LOCATE_ZOOM else null
                    ),
                    animation = LocateAnimation
                )
            }
            return
        }
        isFollowingPosition = true
        zoomedYet = false
        animateToPositionIfFollowing(position, bearing)
    }

    /** Turn navigation mode either on or off. When turned off, resets tilt back to 0 but not
     *  bearing. */
    suspend fun setNavigationMode(value: Boolean, position: LatLon?, bearing: Double?) {
        if (mode !is CameraMode.Browsing) return
        if (isNavigationMode == value) return
        isNavigationMode = value
        if (value) {
            animateToPositionIfFollowing(position, bearing)
        } else {
            map.animateCamera(CameraUpdate(tilt = 0.0), SnapAnimation)
        }
    }

    fun onPan(hasLocation: Boolean) {
        // Panning while a sheet is open does not set isFollowingPosition to false. I.e. it will
        // snap back once exiting the form
        if (mode !is CameraMode.Browsing) return
        // Panning only stops following once a location is displayed; the first fix still centers
        // the map after panning while waiting for it.
        if (!hasLocation) return

        isFollowingPosition = false
    }

    fun onRotate(hasLocation: Boolean) {
        if (mode !is CameraMode.Browsing) return
        if (!hasLocation) return

        // as navigation mode continuously updates the bearing, rotating manually signals the user
        // intent to end this mode (like clicking the compass)
        if (isNavigationMode) isNavigationMode = false
    }

    /** Animate the current camera position to [position] and [bearing] if not null each and if it
     *  is allowed by the current camera mode */
    suspend fun animateToPositionIfFollowing(position: LatLon?, bearing: Double?) {
        if (mode !is CameraMode.Browsing) return
        if (!isFollowingPosition || position == null) return
        val zoom = if (!zoomedYet && map.cameraPosition.zoom < 17.0) LOCATE_ZOOM else null
        zoomedYet = true
        map.animateCamera(
            update = CameraUpdate(
                target = position.toPosition(),
                zoom = zoom,
                bearing = if (isNavigationMode) bearing else null,
                tilt = if (isNavigationMode) 60.0 else null,
                // browsing has no sheet padding
                padding = DpPadding.Zero,
            ),
            animation = LocateAnimation
        )
    }

    /** Resets bearing and tilt to 0, i.e. north-up, no tilt */
    suspend fun resetCompass() {
        // Navigation mode continuously sets bearing and tilt, so pressing the compass button
        // signals the user's intent to stop that
        if (mode is CameraMode.Browsing) {
            isNavigationMode = false
        }
        map.animateCamera(CameraUpdate(bearing = 0.0, tilt = 0.0), SnapAnimation)
    }

    /** Stop the camera from following the position while a sheet is open. */
    fun openSheet() {
        if (mode !is CameraMode.Sheet) mode = CameraMode.Sheet()
    }

    /** Zoom to the given [geometry] of the object shown in the open sheet. */
    suspend fun focus(geometry: ElementGeometry, padding: DpPadding) {
        rememberPositionBeforeSheet()
        map.animateTo(geometry, padding)
    }

    /** Move to the given [position] of the object shown in the open sheet. */
    suspend fun focus(position: LatLon, padding: DpPadding) {
        rememberPositionBeforeSheet()
        map.animateCamera(
            update = CameraUpdate(target = position.toPosition(), padding = padding),
            animation = SnapAnimation
        )
    }

    /** Remembers the camera position the first time a sheet moves the camera, so that the camera
     *  can return to it when the sheet is closed. */
    private fun rememberPositionBeforeSheet() {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.previous == null) mode = sheet.copy(previous = map.cameraPosition)
    }

    /** Change the [padding] without moving the map. */
    fun setPadding(padding: DpPadding) {
        val camera = map.cameraPosition
        map.setCameraPosition(camera.copy(
            target = map.positionAtCenter(padding)?.toPosition() ?: camera.target,
            padding = padding,
        ))
    }

    /** Resume browsing. Returns to the user's [position] and [bearing] when following it, otherwise
     *  to the camera position from before the sheet(s) moved the camera, if any. */
    suspend fun closeSheet(position: LatLon?, bearing: Double?) {
        val sheet = mode as? CameraMode.Sheet ?: return
        mode = CameraMode.Browsing
        // when we follow the user's position, we want to zoom back to the current user's position
        // instead of zoom back to the position from where the sheet was opened
        if (isFollowingPosition && position != null) {
            animateToPositionIfFollowing(position, bearing)
        } else if (sheet.previous != null) {
            // when restoring, keep the user's current bearing and tilt because also rotating and
            // tilting back to where the camera was when the sheet was opened would be too
            // distracting and obstrusive
            map.animateCamera(
                update = CameraUpdate(
                    target = sheet.previous.target,
                    zoom = sheet.previous.zoom,
                    padding = DpPadding.Zero,
                ),
                animation = zoomAnimation(map.cameraPosition.zoom - sheet.previous.zoom),
            )
        } else {
            setPadding(DpPadding.Zero)
        }
    }

    /** Move the camera immediately to the given [position]. */
    fun moveTo(position: CameraPosition) {
        isNavigationMode = false
        isFollowingPosition = false
        map.setCameraPosition(position)
    }

    companion object {
        /** Zoom level when focusing on the user's location */
        private const val LOCATE_ZOOM = 18.0

        /** Camera animation when animating the user's location */
        private val LocateAnimation = CameraAnimation.Ease(600.milliseconds)

        /** Camera animation when animating the tilt or bearing */
        private val SnapAnimation = CameraAnimation.Ease(300.milliseconds)

        /** Camera animation when animating the zoom. Duration depends on how much is zoomed */
        private fun zoomAnimation(zoomDiff: Double) = CameraAnimation.Ease(
            maxOf(300, (abs(zoomDiff) * 300).roundToInt()).milliseconds
        )
    }
}

@Serializable
internal sealed interface CameraMode {
    @Serializable
    data object Browsing : CameraMode

    /** A sheet is open. Saved so that a recreated screen still returns to [previous] when the
     *  sheet is closed. */
    @Serializable
    data class Sheet(
        /** Camera position from before the sheet moved the camera. Null if it did not. */
        val previous: CameraPosition? = null,
    ) : CameraMode
}
