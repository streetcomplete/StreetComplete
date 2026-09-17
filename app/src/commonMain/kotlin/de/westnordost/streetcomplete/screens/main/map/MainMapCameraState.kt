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
    val mode = rememberSerializable {
        mutableStateOf<CameraMode>(CameraMode.Browsing(when {
            initiallyNavigating && initiallyFollowing -> Tracking.Navigating
            initiallyNavigating -> Tracking.NavigationPaused
            initiallyFollowing -> Tracking.Following
            else -> Tracking.Free
        }))
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

    val isFollowingPosition: Boolean get() = (mode as? CameraMode.Browsing)?.tracking?.following == true
    val isNavigationMode: Boolean get() = (mode as? CameraMode.Browsing)?.tracking?.navigating == true

    // TODO: With CameraPosition.padding and destination-padding fits, move sheet padding into camera
    //  updates and saved focus; remove presentation padding and compose-before-restore staging.
    fun padding(sheetPadding: PaddingValues): PaddingValues =
        if ((mode as? CameraMode.Sheet)?.padded == true) sheetPadding else PaddingValues(0.dp)

    suspend fun zoomBy(amount: Double) {
        val camera = map.cameraPosition
        map.animateCameraPosition(camera.copy(zoom = camera.zoom + amount), CameraAnimation.Ease(300.milliseconds))
    }

    suspend fun zoomToCluster(zoom: Double) {
        map.animateCameraPosition(map.cameraPosition.copy(zoom = zoom))
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
        mode = browsing.copy(tracking = if (browsing.tracking.navigating) Tracking.Navigating else Tracking.Following)
        followLocation(location, bearing)
    }

    /** Panning only stops following once a location is displayed; the first fix still centers
     *  the map after panning while waiting for it. */
    fun onPan(hasLocation: Boolean) {
        val browsing = when (val current = mode) {
            is CameraMode.Browsing -> current
            is CameraMode.Restoring -> current.resume
            else -> return
        }
        if (hasLocation) mode = CameraMode.Browsing(
            if (browsing.tracking.navigating) Tracking.NavigationPaused else Tracking.Free
        )
    }

    suspend fun setNavigationMode(value: Boolean, location: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (browsing.tracking.navigating == value) return
        mode = browsing.copy(tracking = if (value) {
            if (browsing.tracking.following) Tracking.Navigating else Tracking.NavigationPaused
        } else {
            if (browsing.tracking.following) Tracking.Following else Tracking.Free
        })
        if (value) followLocation(location, bearing)
        else map.animateCameraPosition(map.cameraPosition.copy(tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
    }

    suspend fun followLocation(location: LatLon?, bearing: Double?) {
        val browsing = mode as? CameraMode.Browsing ?: return
        if (!browsing.tracking.following || location == null) return
        val camera = map.cameraPosition
        val zoom = if (!browsing.zoomedYet && camera.zoom < 17.0) 18.0 else camera.zoom
        mode = browsing.copy(zoomedYet = true)
        map.animateCameraPosition(camera.copy(
            target = location.toPosition(),
            zoom = zoom,
            bearing = if (browsing.tracking.navigating) bearing ?: camera.bearing else camera.bearing,
            tilt = if (browsing.tracking.navigating) 60.0 else camera.tilt,
        ), CameraAnimation.Ease(600.milliseconds))
    }

    suspend fun resetCompass() {
        val browsing = when (val current = mode) {
            is CameraMode.Browsing -> current
            is CameraMode.Restoring -> current.resume
            else -> null
        }
        if (browsing != null) mode = browsing.copy(
            tracking = if (browsing.tracking.following) Tracking.Following else Tracking.Free
        )
        map.animateCameraPosition(map.cameraPosition.copy(bearing = 0.0, tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
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
        mode = sheet.copy(focused = true,
            previous = sheet.previous ?: FocusCamera(camera.target.toLatLon(), camera.zoom))
        focus(geometry, sheet.resume.tracking.navigating)
    }

    suspend fun composeNote(id: String, position: LatLon) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        mode = sheet.copy(focused = true)
        val camera = map.cameraPosition
        map.animateCameraPosition(camera.copy(target = position.toPosition(),
            tilt = if (sheet.resume.tracking.navigating) 0.0 else camera.tilt), CameraAnimation.Ease(300.milliseconds))
    }

    /** Keep the crosshair over the same position when opening a form changes the map padding. */
    fun preserveCrosshairPosition(position: LatLon) {
        map.setCameraPosition(map.cameraPosition.copy(target = position.toPosition()))
    }

    fun openEditHistory(key: EditKey?) {
        val previous = mode
        if (previous is CameraMode.EditHistory && previous.key == key) return
        mode = CameraMode.EditHistory(key, previous.browsing)
    }

    suspend fun focusEdit(key: EditKey, geometry: ElementGeometry) {
        val history = mode as? CameraMode.EditHistory ?: return
        if (history.key != key || history.focused) return
        mode = history.copy(focused = true)
        focus(geometry, history.resume.tracking.navigating)
    }

    fun closeInspection() {
        val inspection = mode
        if (inspection is CameraMode.Browsing || inspection is CameraMode.Restoring) return
        mode = CameraMode.Restoring(inspection.browsing.copy(zoomedYet = false),
            (inspection as? CameraMode.Sheet)?.previous)
    }

    /** Run after composing the unpadded map; fixes remain ignored until restoration finishes. */
    internal suspend fun restore(location: LatLon?, bearing: Double?) {
        val restoring = mode as? CameraMode.Restoring ?: return
        val camera = map.cameraPosition
        val resume = restoring.resume
        if (resume.tracking == Tracking.Navigating && location != null) {
            map.animateCameraPosition(camera.copy(
                target = location.toPosition(),
                zoom = if (camera.zoom < 17.0) 18.0 else camera.zoom,
                bearing = bearing ?: camera.bearing,
                tilt = 60.0,
            ), CameraAnimation.Ease(600.milliseconds))
        } else if (restoring.previous != null) {
            val previous = restoring.previous
            // Restore the pre-focus target and zoom, keeping the user's current bearing and tilt.
            map.animateCameraPosition(
                camera.copy(target = previous.position.toPosition(), zoom = previous.zoom),
                CameraAnimation.Ease(maxOf(300, (abs(camera.zoom - previous.zoom) * 300).roundToInt()).milliseconds),
            )
        }
        if (mode == restoring) mode = resume
    }

    fun moveTo(position: CameraPosition) {
        mode = when (val current = mode) {
            is CameraMode.Browsing, is CameraMode.Restoring -> CameraMode.Browsing(Tracking.Free)
            is CameraMode.Sheet -> current.copy(resume = CameraMode.Browsing(Tracking.Free))
            is CameraMode.EditHistory -> current.copy(resume = CameraMode.Browsing(Tracking.Free))
        }
        // MapState retains this update even before a map surface is attached.
        map.setCameraPosition(position)
    }

    suspend fun inspectSheet(id: String) {
        val sheet = mode as? CameraMode.Sheet ?: return
        if (sheet.id != id || sheet.focused) return
        mode = sheet.copy(focused = true)
        flattenNavigation(sheet.resume)
    }

    suspend fun inspectEditHistory() {
        val history = mode as? CameraMode.EditHistory ?: return
        if (history.key != null || history.focused) return
        mode = history.copy(focused = true)
        flattenNavigation(history.resume)
    }

    private suspend fun flattenNavigation(resume: CameraMode.Browsing) {
        if (resume.tracking.navigating) {
            map.animateCameraPosition(map.cameraPosition.copy(tilt = 0.0), CameraAnimation.Ease(300.milliseconds))
        }
    }

    private suspend fun focus(geometry: ElementGeometry, flatten: Boolean) {
        val camera = map.cameraPosition
        val tilt = if (flatten) 0.0 else camera.tilt
        val fitted = map.cameraForGeometry(geometry.toGeometry(), camera.bearing, tilt)
        // zoom in a bit less than fully to keep a margin around the element, and not too far for points
        val targetZoom = min(fitted.zoom - 0.75, 19.0)
        val zoomDiff = abs(camera.zoom - targetZoom)
        map.animateCameraPosition(
            camera.copy(
                target = fitted.target,
                tilt = tilt,
                // only zoom if the difference is big enough
                zoom = if (zoomDiff > 0.5) targetZoom else camera.zoom,
            ),
            CameraAnimation.Ease(maxOf(450, (zoomDiff * 450).roundToInt()).milliseconds),
        )
    }

}

@Serializable
internal sealed interface CameraMode {
    @Serializable
    data class Browsing(val tracking: Tracking, val zoomedYet: Boolean = false) : CameraMode

    @Serializable
    data class Sheet(
        val id: String,
        val padded: Boolean,
        val resume: Browsing,
        val previous: FocusCamera? = null,
        val focused: Boolean = false,
    ) : CameraMode

    @Serializable
    data class Restoring(val resume: Browsing, val previous: FocusCamera?) : CameraMode

    @Serializable
    data class EditHistory(val key: EditKey?, val resume: Browsing, val focused: Boolean = false) : CameraMode
}

private val CameraMode.browsing: CameraMode.Browsing get() = when (this) {
    is CameraMode.Browsing -> this
    is CameraMode.Sheet -> resume
    is CameraMode.EditHistory -> resume
    is CameraMode.Restoring -> resume
}

/** Panning pauses position following without leaving navigation mode, as on Android. */
@Serializable
internal enum class Tracking(val following: Boolean, val navigating: Boolean) {
    Free(false, false), Following(true, false), Navigating(true, true), NavigationPaused(false, true)
}

@Serializable
internal data class FocusCamera(val position: LatLon, val zoom: Double)
