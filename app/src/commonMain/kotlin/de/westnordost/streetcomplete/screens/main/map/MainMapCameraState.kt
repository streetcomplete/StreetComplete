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
        map.zoomBy(amount, SnapAnimation)
    }

    /** Start following the current position.
     *  Special case when a sheet is open: Just zoom to the position, don't start to follow it. */
    suspend fun followPosition(position: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing
        if (browsing == null) {
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
        mode = browsing.copy(isFollowingPosition = true)
        animateToPositionIfFollowing(position, bearing)
    }

    /** Turn navigation mode either on or off. When turned off, resets tilt back to 0 but not
     *  bearing. */
    suspend fun setNavigationMode(value: Boolean, position: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (browsing.isNavigationMode == value) return
        mode = browsing.copy(isNavigationMode = value)
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
        val browsing = when (val current = mode) {
            is CameraMode.Browsing -> current
            is CameraMode.Restoring -> current.resume
            else -> return
        }
        // Panning only stops following once a location is displayed; the first fix still centers
        // the map after panning while waiting for it.
        if (!hasLocation) return

        mode = browsing.copy(isFollowingPosition = false)
    }

    /** Animate the current camera position to [position] and [bearing] if not null each and if it
     *  is allowed by the current camera mode */
    suspend fun animateToPositionIfFollowing(position: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (!browsing.isFollowingPosition || position == null) return
        val camera = map.cameraPosition
        val zoom = if (!browsing.zoomedYet && camera.zoom < 17.0) LOCATE_ZOOM else camera.zoom
        mode = browsing.copy(zoomedYet = true)
        map.animateCameraPosition(
            position = camera.copy(
                target = position.toPosition(),
                zoom = zoom,
                bearing = if (browsing.isNavigationMode) bearing ?: camera.bearing else camera.bearing,
                tilt = if (browsing.isNavigationMode) 60.0 else camera.tilt,
            ),
            animation = LocateAnimation
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
            resume = previous.browsing,
            previous = (previous as? CameraMode.Sheet)?.previous
        )
    }

    suspend fun focusSheet(id: String, geometry: ElementGeometry) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        val camera = map.cameraPosition
        mode = sheet.copy(
            focused = true,
            previous = sheet.previous ?: FocusCamera(camera.target.toLatLon(), camera.zoom)
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
                tilt = if (sheet.resume.isNavigationMode) 0.0 else camera.tilt
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
        mode = CameraMode.EditHistory(key, previous.browsing)
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
            resume = inspection.browsing.copy(zoomedYet = false),
            previous = (inspection as? CameraMode.Sheet)?.previous
        )
    }

    /** Returning to a previous [position] and [bearing] after a sheet has been focused. */
    internal suspend fun restore(position: LatLon?, bearing: Double?) {
        val restoring = mode as? CameraMode.Restoring ?: return
        val camera = map.cameraPosition
        val resume = restoring.resume
        if (resume.isFollowingPosition && position != null) {
            animateToPositionIfFollowing(position, bearing)
        } else if (restoring.previous != null) {
            val previous = restoring.previous
            // Restore the pre-focus target and zoom, keeping the user's current bearing and tilt.
            map.animateCameraPosition(
                position = camera.copy(
                    target = previous.position.toPosition(),
                    zoom = previous.zoom
                ),
                animation = zoomAnimation(camera.zoom - previous.zoom),
            )
        }
        if (mode == restoring) mode = resume
    }

    /** Move the camera immediately to the given [position]. */
    fun moveTo(position: CameraPosition) {
        val freeBrowsing = CameraMode.Browsing(false, false)
        // this also stops any position-following and navigation mode
        mode = when (val current = mode) {
            is CameraMode.Browsing,
            is CameraMode.Restoring -> freeBrowsing
            is CameraMode.Sheet -> current.copy(resume = freeBrowsing)
            is CameraMode.EditHistory -> current.copy(resume = freeBrowsing)
        }
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
    data class Browsing(
        /** Whether the camera is following the user's position */
        val isFollowingPosition: Boolean,
        /** Whether when the camera is following the user's position the map is tilted and the
         *  bearing is updated according to the user's direction */
        val isNavigationMode: Boolean,
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
