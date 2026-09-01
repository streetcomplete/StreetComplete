package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.initialBearingTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MapPresentationDetachedException
import org.maplibre.compose.map.MapState
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * State and camera policy for StreetComplete's shared main map.
 *
 * The class deliberately owns interaction policy, while [MainMap] owns rendering. This keeps the
 * behavior available to every Compose Multiplatform entry point without exposing a platform map
 * view or fragment.
 */
@Stable
class MainMapState internal constructor(
    val mapState: MapState,
    private val controller: MainMapCameraController,
) {
    val isFollowingPosition: Boolean get() = controller.isFollowingPosition
    val isNavigationMode: Boolean get() = controller.isNavigationMode
    val userHasMovedCamera: Boolean get() = controller.userHasMovedCamera
    val displayedLocation: Location? get() = controller.displayedLocation
    val cameraPosition: CameraPosition get() = mapState.cameraPosition

    fun setFollowingPosition(value: Boolean) = controller.updateFollowingPosition(value)
    fun setNavigationMode(value: Boolean) = controller.updateNavigationMode(value)
    fun zoomIn() = controller.zoomBy(1.0)
    fun zoomOut() = controller.zoomBy(-1.0)
    fun zoomByDrag(dp: Float) = controller.zoomBy(dp / 20.0)
    fun resetCompass() = controller.resetCompass()

    internal fun onLocationChanged(location: Location?, track: List<LatLon>) =
        controller.onLocationChanged(location, track)

    internal fun onCameraChanged(
        position: CameraPosition,
        moveReason: CameraMoveReason,
        isMoving: Boolean,
    ) = controller.onCameraChanged(position, moveReason, isMoving)

    internal fun save() = controller.save()
}

/** Remembers the durable state and exact legacy camera policy used by [MainMap]. */
@Composable
fun rememberMainMapState(
    preferences: Preferences = koinInject(),
): MainMapState {
    val persistedState = remember(preferences) { PreferencesMapCameraState(preferences) }
    val mapState = rememberStreetCompleteMapState(persistedState.loadCamera())
    val scope = rememberCoroutineScope()
    val state = remember(mapState, persistedState, scope) {
        MainMapState(
            mapState,
            MainMapCameraController(
                camera = MapLibreCamera(mapState),
                persistedState = persistedState,
                scope = scope,
            ),
        )
    }
    DisposableEffect(state) {
        onDispose(state::save)
    }
    return state
}

internal interface MainMapCamera {
    val position: CameraPosition
    suspend fun animateTo(position: CameraPosition, duration: Duration)
}

private class MapLibreCamera(private val state: MapState) : MainMapCamera {
    override val position: CameraPosition get() = state.cameraPosition

    override suspend fun animateTo(position: CameraPosition, duration: Duration) {
        val presentation = state.presentation ?: return
        try {
            presentation.animateCameraPosition(position, duration)
        } catch (_: MapPresentationDetachedException) {
            // A replacement presentation will receive the durable MapState camera automatically.
        }
    }
}

internal data class PersistedMapCameraState(
    val camera: CameraPosition,
    val isFollowingPosition: Boolean,
    val isNavigationMode: Boolean,
)

internal interface MainMapPersistedState {
    fun loadCamera(): CameraPosition
    fun loadIsFollowingPosition(): Boolean
    fun loadIsNavigationMode(): Boolean
    fun save(state: PersistedMapCameraState)
}

private class PreferencesMapCameraState(private val preferences: Preferences) : MainMapPersistedState {
    override fun loadCamera() = CameraPosition(
        bearing = preferences.mapRotation,
        target = preferences.mapPosition.toPosition(),
        tilt = preferences.mapTilt,
        zoom = preferences.mapZoom,
    )

    override fun loadIsFollowingPosition() = preferences.mapIsFollowing
    override fun loadIsNavigationMode() = preferences.mapIsNavigationMode

    override fun save(state: PersistedMapCameraState) {
        preferences.mapPosition = state.camera.target.toLatLon()
        preferences.mapRotation = state.camera.bearing
        preferences.mapTilt = state.camera.tilt
        preferences.mapZoom = state.camera.zoom
        preferences.mapIsFollowing = state.isFollowingPosition
        preferences.mapIsNavigationMode = state.isNavigationMode
    }
}

@Stable
internal class MainMapCameraController(
    private val camera: MainMapCamera,
    private val persistedState: MainMapPersistedState,
    private val scope: CoroutineScope,
) {
    var isFollowingPosition by mutableStateOf(persistedState.loadIsFollowingPosition())
        private set
    var isNavigationMode by mutableStateOf(persistedState.loadIsNavigationMode())
        private set
    var userHasMovedCamera by mutableStateOf(false)
        private set
    var displayedLocation by mutableStateOf<Location?>(null)
        private set

    private var track: List<LatLon> = emptyList()
    private var zoomedYet = false
    private var lastObservedPosition = camera.position

    fun updateFollowingPosition(value: Boolean) {
        if (isFollowingPosition == value) return
        isFollowingPosition = value
        if (!value) {
            zoomedYet = false
        } else {
            centerCurrentPosition()
        }
    }

    fun updateNavigationMode(value: Boolean) {
        if (isNavigationMode == value) return
        isNavigationMode = value
        if (value) {
            if (isFollowingPosition) centerCurrentPosition()
        } else {
            animateCamera(300.milliseconds) { copy(tilt = 0.0) }
        }
    }

    fun resetCompass() {
        val wasNavigating = isNavigationMode
        if (wasNavigating) isNavigationMode = false
        animateCamera(300.milliseconds) {
            copy(bearing = 0.0, tilt = 0.0)
        }
    }

    fun zoomBy(delta: Double) {
        animateCamera(300.milliseconds) { copy(zoom = zoom + delta) }
    }

    fun onLocationChanged(location: Location?, track: List<LatLon>) {
        displayedLocation = location
        this.track = track
        if (location == null) {
            updateNavigationMode(false)
        } else if (isFollowingPosition) {
            centerCurrentPosition()
        }
    }

    fun onCameraChanged(
        position: CameraPosition,
        moveReason: CameraMoveReason,
        isMoving: Boolean,
    ) {
        if (moveReason == CameraMoveReason.GESTURE) {
            userHasMovedCamera = true
            // MapLibre Compose currently has no pan-begin callback. Target movement is the narrowest
            // common signal that preserves the legacy rule: pan stops follow, zoom/rotate/tilt do not.
            if (displayedLocation != null && position.target != lastObservedPosition.target) {
                updateFollowingPosition(false)
            }
        }
        lastObservedPosition = position
        if (!isMoving) save(position)
    }

    fun save() = save(camera.position)

    private fun save(position: CameraPosition) {
        persistedState.save(
            PersistedMapCameraState(position, isFollowingPosition, isNavigationMode)
        )
    }

    private fun centerCurrentPosition() {
        val location = displayedLocation ?: return
        animateCamera(600.milliseconds) {
            val navigationBearing = if (isNavigationMode) getTrackBearingFromPositions(track) else null
            val targetZoom = if (!zoomedYet && zoom < 17.0) 18.0 else zoom
            zoomedYet = true
            copy(
                target = location.position.toPosition(),
                bearing = navigationBearing ?: bearing,
                tilt = if (isNavigationMode) 60.0 else tilt,
                zoom = targetZoom,
            )
        }
    }

    private fun animateCamera(
        duration: Duration,
        transform: CameraPosition.() -> CameraPosition,
    ) {
        scope.launch { camera.animateTo(camera.position.transform(), duration) }
    }
}

private fun Position.toLatLon() = LatLon(latitude = latitude, longitude = longitude)

/** Utility function to estimate current bearing from a track. */
internal fun getTrackBearingFromPositions(track: List<LatLon>): Double? {
    val last = track.lastOrNull() ?: return null
    val point = track.findLast { it.distanceTo(last) > MIN_TRACK_DISTANCE_FOR_BEARING } ?: return null
    return point.initialBearingTo(last)
}

private const val MIN_TRACK_DISTANCE_FOR_BEARING = 15f
