package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val mode = rememberSerializable {
        mutableStateOf<CameraMode>(CameraMode.Browsing(
            isFollowingPosition = initiallyFollowing,
            isNavigationMode = initiallyNavigating
        ))
    }
    return remember(mapState) { MainMapCameraState(mapState, mode) }
}

/** Owns camera transitions; MapLibre owns the position and the running animation. */
class MainMapCameraState internal constructor(
    private val map: MapState,
    mode: MutableState<CameraMode>,
) {
    private var mode by mode

    internal val inspection: CameraMode? get() = mode.takeUnless { it is CameraMode.Browsing }

    val isFollowingPosition: Boolean get() = (mode as? CameraMode.Browsing)?.isFollowingPosition == true
    val isNavigationMode: Boolean get() = (mode as? CameraMode.Browsing)?.isNavigationMode == true

    // TODO: With CameraPosition.padding and destination-padding fits, move sheet padding into camera
    //  updates and saved focus; remove presentation padding and compose-before-restore staging.
    fun padding(sheetPadding: PaddingValues): PaddingValues =
        if ((mode as? CameraMode.Sheet)?.padded == true) sheetPadding else PaddingValues(0.dp)

    suspend fun zoomBy(amount: Double) {
        map.zoomBy(amount)
    }

    suspend fun locate(location: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing
        if (browsing == null) {
            // An explicit location click can recenter an open form without resuming GPS following.
            if (location != null) {
                val camera = map.cameraPosition
                map.animateCameraPosition(camera.copy(target = location.toPosition(),
                    zoom = if (camera.zoom < 17.0) 18.0 else camera.zoom), CameraAnimation.Ease(600.milliseconds))
            }
            return
        }
        mode = browsing.copy(isFollowingPosition = true)
        animateToPositionIfFollowing(location, bearing)
    }

    /** Panning only stops following once a location is displayed; the first fix still centers
     *  the map after panning while waiting for it. */
    fun onPan(hasLocation: Boolean) {
        val browsing = when (val current = mode) {
            is CameraMode.Browsing -> current
            is CameraMode.Restoring -> current.resume
            else -> return
        }
        if (hasLocation) {
            mode = CameraMode.Browsing(isFollowingPosition = false)
        }
    }

    suspend fun setNavigationMode(value: Boolean, location: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (browsing.isNavigationMode == value) return
        mode = browsing.copy(isNavigationMode = true)
        if (value) animateToPositionIfFollowing(location, bearing)
        else map.animateCameraPosition(map.cameraPosition.copy(tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
    }

    /** Animate the current camera position to [position] and [bearing] if not null each and if it
     *  is allowed by the current camera mode */
    suspend fun animateToPositionIfFollowing(position: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (!browsing.isFollowingPosition || position == null) return
        val camera = map.cameraPosition
        val zoom = if (!browsing.zoomedYet && camera.zoom < 17.0) 18.0 else camera.zoom
        mode = browsing.copy(zoomedYet = true)
        map.animateCameraPosition(
            position = camera.copy(
                target = position.toPosition(),
                zoom = zoom,
                bearing = if (browsing.isNavigationMode) bearing ?: camera.bearing else camera.bearing,
                tilt = if (browsing.isNavigationMode) 60.0 else camera.tilt,
            ),
            animation = CameraAnimation.Ease(600.milliseconds)
        )
    }

    /** Resets bearing and tilt to 0, i.e. north-up, no tilt */
    suspend fun resetCompass() {
        val browsing = when (val current = mode) {
            is CameraMode.Browsing -> current
            is CameraMode.Restoring -> current.resume
            else -> null
        }
        // Navigation mode continuously sets bearing and tilt, so pressing the compass button
        // signals the user's intent to stop that
        if (browsing != null) {
            mode = browsing.copy(isNavigationMode = false)
        }
        map.animateCameraPosition(
            position = map.cameraPosition.copy(bearing = 0.0, tilt = 0.0),
            animation = CameraAnimation.Ease(300.milliseconds)
        )
    }

    /** Enter immediately, before the selected form has finished loading, to stop following. */
    fun openSheet(id: String, padded: Boolean) {
        val previous = mode
        if (previous is CameraMode.Sheet && previous.id == id) {
            mode = previous.copy(padded = padded)
            return
        }
        mode = CameraMode.Sheet(id, padded, previous.browsing,
            previous = (previous as? CameraMode.Sheet)?.previous)
    }

    suspend fun focusSheet(id: String, geometry: ElementGeometry) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        val camera = map.cameraPosition
        mode = sheet.copy(
            focused = true,
            previous = sheet.previous ?: FocusCamera(camera.target.toLatLon(), camera.zoom)
        )
        map.animateTo(geometry, sheet.resume.isNavigationMode)
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
        map.animateCameraPosition(camera.copy(target = position.toPosition(),
            tilt = if (sheet.resume.isNavigationMode) 0.0 else camera.tilt), CameraAnimation.Ease(300.milliseconds))
    }

    /** Keep the crosshair over the same position when opening a form changes the map padding. */
    fun preserveCrosshairPosition(position: LatLon) {
        map.setCameraPosition(map.cameraPosition.copy(target = position.toPosition()))
    }

    fun openEditHistory(key: EditKey) {
        val previous = mode
        if (previous is CameraMode.EditHistory && previous.key == key) return
        mode = CameraMode.EditHistory(key, previous.browsing)
    }

    suspend fun focusEdit(key: EditKey, geometry: ElementGeometry) {
        val history = mode as? CameraMode.EditHistory ?: return
        if (history.key != key || history.focused) return
        mode = history.copy(focused = true)
        map.animateTo(geometry, history.resume.isNavigationMode)
    }

    fun closeInspection() {
        val inspection = mode
        if (inspection is CameraMode.Browsing || inspection is CameraMode.Restoring) return
        mode = CameraMode.Restoring(inspection.browsing.copy(zoomedYet = false),
            (inspection as? CameraMode.Sheet)?.previous)
    }

    /** Returning to a previous [position] and [bearing] after a sheet has been focussed. */
    internal suspend fun restore(position: LatLon?, bearing: Double?) {
        val restoring = mode as? CameraMode.Restoring ?: return
        val camera = map.cameraPosition
        val resume = restoring.resume
        if (resume.isFollowingPosition && resume.isNavigationMode && position != null) {
            map.animateCameraPosition(
                position = camera.copy(
                    target = position.toPosition(),
                    zoom = if (camera.zoom < 17.0) 18.0 else camera.zoom,
                    bearing = bearing ?: camera.bearing,
                    tilt = 60.0,
                ),
                animation = CameraAnimation.Ease(600.milliseconds)
            )
        } else if (restoring.previous != null) {
            val previous = restoring.previous
            // Restore the pre-focus target and zoom, keeping the user's current bearing and tilt.
            map.animateCameraPosition(
                position = camera.copy(target = previous.position.toPosition(), zoom = previous.zoom),
                animation = CameraAnimation.Ease(maxOf(300, (abs(camera.zoom - previous.zoom) * 300).roundToInt()).milliseconds),
            )
        }
        if (mode == restoring) mode = resume
    }

    /** Move the camera immediately to the given [position]. */
    fun moveTo(position: CameraPosition) {
        mode = when (val current = mode) {
            is CameraMode.Browsing,
            is CameraMode.Restoring -> CameraMode.Browsing()
            is CameraMode.Sheet -> current.copy(resume = CameraMode.Browsing())
            is CameraMode.EditHistory -> current.copy(resume = CameraMode.Browsing())
        }
        map.setCameraPosition(position)
    }
}

@Serializable
internal sealed interface CameraMode {
    /** Identifies what is inspected. Unlike the mode itself, it does not change with the progress
     *  of that inspection, so it can key an effect that runs once per inspected object. */
    val inspectionKey: String?

    @Serializable
    data class Browsing(
        /** Whether the camera is following the user's position */
        val isFollowingPosition: Boolean = false,
        /** Whether when the camera is following the user's position the map is tilted and the
         *  bearing is updated according to the user's direction */
        val isNavigationMode: Boolean = false,
        /** Whether after starting to follow the user's position, the camera has already zoomed to
         *  an appropriate zoom level once. */
        val zoomedYet: Boolean = false
    ) : CameraMode {
        override val inspectionKey get() = null
    }

    @Serializable
    data class Sheet(
        val id: String,
        val padded: Boolean,
        val resume: Browsing,
        val previous: FocusCamera? = null,
        val focused: Boolean = false,
    ) : CameraMode {
        override val inspectionKey get() = "sheet $id"
    }

    @Serializable
    data class Restoring(
        val resume: Browsing,
        val previous: FocusCamera?
    ) : CameraMode {
        override val inspectionKey get() = "restoring"
    }

    @Serializable
    data class EditHistory(
        val key: EditKey,
        val resume: Browsing,
        val focused: Boolean = false
    ) : CameraMode {
        override val inspectionKey get() = "edit history $key"
    }
}

private val CameraMode.browsing: CameraMode.Browsing get() = when (this) {
    is CameraMode.Browsing -> this
    is CameraMode.Sheet -> resume
    is CameraMode.EditHistory -> resume
    is CameraMode.Restoring -> resume
}

@Serializable
internal data class FocusCamera(val position: LatLon, val zoom: Double)
