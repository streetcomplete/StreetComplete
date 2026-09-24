package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toPosition
import kotlinx.serialization.Serializable
import org.maplibre.compose.camera.CameraAnimation
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

    internal val inspection: CameraMode? get() = mode.takeUnless { it is CameraMode.Browsing }

    var isFollowingPosition: Boolean by isFollowingPositionState
        private set

    var isNavigationMode: Boolean by isNavigationModeState
        private set

    private var zoomedYet by zoomedYet

    // TODO: With CameraPosition.padding and destination-padding fits, move sheet padding into camera
    //  updates and saved focus; remove presentation padding and compose-before-restore staging.
    fun padding(sheetPadding: PaddingValues): PaddingValues =
        if ((mode as? CameraMode.Sheet)?.padded == true) sheetPadding else PaddingValues(0.dp)

    suspend fun zoomBy(amount: Double) {
        map.zoomBy(amount, SnapAnimation)
    }

    /** Start following the current position.
     *  Special case when a sheet is open: Just zoom to the position, don't start to follow it. */
    suspend fun followPosition(position: LatLon?, bearing: Double?) {
        if (mode !is CameraMode.Browsing) {
            // An explicit location click can recenter an open form without resuming GPS following.
            if (position != null) {
                val camera = map.cameraPosition
                map.animateCameraPosition(
                    position = camera.copy(
                        target = position.toPosition(),
                        zoom = if (camera.zoom < 17.0) LOCATE_ZOOM else camera.zoom
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
            map.animateCameraPosition(
                position = map.cameraPosition.copy(tilt = 0.0),
                animation = SnapAnimation
            )
        }
    }

    fun onPan(hasLocation: Boolean) {
        // Panning while a sheet is open does not set isFollowingPosition to false. I.e. it will
        // snap back once exiting the form
        if (mode !is CameraMode.Browsing && mode !is CameraMode.Restoring) return
        // Panning only stops following once a location is displayed; the first fix still centers
        // the map after panning while waiting for it.
        if (!hasLocation) return

        isFollowingPosition = false
    }

    fun onRotate(hasLocation: Boolean) {
        if (mode !is CameraMode.Browsing && mode !is CameraMode.Restoring) return
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
        val camera = map.cameraPosition
        val zoom = if (!zoomedYet && camera.zoom < 17.0) LOCATE_ZOOM else camera.zoom
        zoomedYet = true
        map.animateCameraPosition(
            position = camera.copy(
                target = position.toPosition(),
                zoom = zoom,
                bearing = if (isNavigationMode) bearing ?: camera.bearing else camera.bearing,
                tilt = if (isNavigationMode) 60.0 else camera.tilt,
            ),
            animation = LocateAnimation
        )
    }

    /** Resets bearing and tilt to 0, i.e. north-up, no tilt */
    suspend fun resetCompass() {
        // Navigation mode continuously sets bearing and tilt, so pressing the compass button
        // signals the user's intent to stop that
        if (mode is CameraMode.Browsing || mode is CameraMode.Restoring) {
            isNavigationMode = false
        }
        map.animateCameraPosition(
            position = map.cameraPosition.copy(bearing = 0.0, tilt = 0.0),
            animation = SnapAnimation
        )
    }

    /** Suspends the current browsing mode */
    fun openSheet(id: String, padded: Boolean) {
        val previous = mode
        if (previous is CameraMode.Sheet && previous.id == id) {
            mode = previous.copy(padded = padded)
            return
        }
        mode = CameraMode.Sheet(
            id = id,
            padded = padded,
            previous = (previous as? CameraMode.Sheet)?.previous
        )
    }

    suspend fun focusSheet(id: String, geometry: ElementGeometry) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        mode = sheet.copy(
            focused = true,
            previous = sheet.previous ?: map.cameraPosition
        )
        map.animateTo(
            geometry = geometry,
            animation = { zoomAnimation(it) }
        )
    }

    suspend fun inspectSheet(id: String) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        mode = sheet.copy(focused = true)
    }

    suspend fun composeNote(id: String, position: LatLon) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        mode = sheet.copy(focused = true)
        val camera = map.cameraPosition
        map.animateCameraPosition(
            position = camera.copy(
                target = position.toPosition(),
                tilt = if (isNavigationMode) 0.0 else camera.tilt
            ),
            animation = SnapAnimation
        )
    }

    /** Keep the crosshair over the same position when opening a form changes the map padding. */
    fun preserveCrosshairPosition(position: LatLon) {
        map.setCameraPosition(map.cameraPosition.copy(target = position.toPosition()))
    }

    fun openEditHistory(key: EditKey) {
        val previous = mode
        if (previous is CameraMode.EditHistory && previous.key == key) return
        mode = CameraMode.EditHistory(key)
    }

    suspend fun focusEdit(key: EditKey, geometry: ElementGeometry) {
        val history = mode as? CameraMode.EditHistory ?: return
        if (history.key != key || history.focused) return
        mode = history.copy(focused = true)
        map.animateTo(
            geometry = geometry,
            animation = { zoomAnimation(it) }
        )
    }

    fun closeInspection() {
        val inspection = mode
        if (inspection is CameraMode.Browsing || inspection is CameraMode.Restoring) return
        mode = CameraMode.Restoring(
            previous = (inspection as? CameraMode.Sheet)?.previous
        )
    }

    /** Returning to a previous [position] and [bearing] after a sheet has been focused. */
    internal suspend fun restore(position: LatLon?, bearing: Double?) {
        val restoring = mode as? CameraMode.Restoring ?: return
        val camera = map.cameraPosition
        // when we follow the user's position, we want to zoom back to the current user's position
        // instead of zoom back to the position from where the sheet was opened
        if (isFollowingPosition && position != null) {
            animateToPositionIfFollowing(position, bearing)
        } else if (restoring.previous != null) {
            // when restoring, keep the user's current bearing and tilt because also rotating and
            // tilting back to where the camera was when the sheet was opened would be too
            // distracting and obstrusive
            map.animateCameraPosition(
                position = camera.copy(
                    target = restoring.previous.target,
                    zoom = restoring.previous.zoom,
                ),
                animation = zoomAnimation(camera.zoom - restoring.previous.zoom),
            )
        }
        if (mode == restoring) mode = CameraMode.Browsing
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
    /** Identifies what is inspected. Unlike the mode itself, it does not change with the progress
     *  of that inspection, so it can key an effect that runs once per inspected object. */
    val inspectionKey: String?

    @Serializable
    data object Browsing : CameraMode {
        override val inspectionKey get() = null
    }

    @Serializable
    data class Sheet(
        val id: String,
        val padded: Boolean,
        val previous: CameraPosition? = null,
        val focused: Boolean = false,
    ) : CameraMode {
        override val inspectionKey get() = "sheet $id"
    }

    @Serializable
    data class Restoring(
        val previous: CameraPosition?
    ) : CameraMode {
        override val inspectionKey get() = "restoring"
    }

    @Serializable
    data class EditHistory(
        val key: EditKey,
        val focused: Boolean = false
    ) : CameraMode {
        override val inspectionKey get() = "edit history $key"
    }
}
