package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.ktx.toLocation
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.initialBearingTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapState
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.tan
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
    private val tracks: MainMapTrackState,
    private val content: MainMapContentState,
    internal val styleConfiguration: MainMapStyleConfiguration,
) {
    init {
        controller.onLocationRestored(
            tracks.displayedMeasurement?.toLocation(),
            tracks.currentTrack.map(Trackpoint::position),
        )
    }

    val isFollowingPosition: Boolean get() = controller.isFollowingPosition
    val isNavigationMode: Boolean get() = controller.isNavigationMode
    val userHasMovedCamera: Boolean get() = controller.userHasMovedCamera
    val displayedLocation: Location? get() = controller.displayedLocation
    val cameraPosition: CameraPosition get() = mapState.cameraPosition
    val cameraPadding: PaddingValues get() = controller.cameraPadding
    val metersPerDp: Double get() = mapState.viewport?.metersPerDpAtTarget ?: 0.0
    val displayedArea: de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox?
        get() = mapState.viewport?.visibleBoundingBox?.toStreetCompleteBoundingBox()
    val displayedMeasurement: LocationMeasurement? get() = tracks.displayedMeasurement
    val isRecordingTrack: Boolean get() = tracks.isRecording
    val currentRenderedTrack: List<LatLon> get() = tracks.currentRenderedTrack
    val oldRenderedTracks: List<List<LatLon>> get() = tracks.oldRenderedTracks
    val highlightedGeometry: ElementGeometry? get() = content.highlightedGeometry
    val markers: List<Marker> get() = content.markers
    val selectedPins: SelectedMapPins? get() = content.selectedPins
    val pinMode: MainMapPinMode get() = content.pinMode
    val showPins: Boolean get() = content.showPins
    val showStyleableOverlay: Boolean get() = content.showStyleableOverlay

    fun setFollowingPosition(value: Boolean) = controller.updateFollowingPosition(value)
    fun setNavigationMode(value: Boolean) = controller.updateNavigationMode(value)
    fun zoomIn() = controller.zoomBy(1.0)
    fun zoomOut() = controller.zoomBy(-1.0)
    fun zoomByDrag(dp: Float) = controller.zoomBy(dp / 20.0)
    fun resetCompass() = controller.resetCompass()
    fun moveTo(
        position: LatLon,
        zoom: Double? = null,
        padding: PaddingValues = cameraPadding,
        duration: Duration = 300.milliseconds,
    ) = controller.moveTo(position, zoom, padding, duration)
    fun fitCluster(positions: List<LatLon>) = controller.fitCluster(positions)
    fun startFocus(geometry: ElementGeometry, padding: PaddingValues = PaddingValues(0.dp)) =
        controller.startFocus(geometry, padding)
    fun clearFocus() = controller.clearFocus()
    fun endFocus() = controller.endFocus()

    fun positionAt(offset: DpOffset): LatLon? =
        mapState.positionFromScreenLocation(offset)?.toLatLon()

    fun offsetOf(position: LatLon): DpOffset? =
        mapState.screenLocationFromPosition(position.toPosition())

    fun clickRadiusInMeters(
        position: LatLon,
        offset: DpOffset,
        radius: Dp = MAP_CLICK_RADIUS,
    ): Double? {
        val edge = positionAt(DpOffset(offset.x + radius, offset.y)) ?: return null
        return position.distanceTo(edge)
    }

    fun onLocationEvent(event: LocationEvent) {
        when (event) {
            is LocationEvent.Update -> tracks.onLocationMeasurement(event.measurement)
            is LocationEvent.Unavailable -> tracks.onLocationUnavailable()
        }
        controller.onLocationChanged(
            tracks.displayedMeasurement?.toLocation(),
            tracks.currentTrack.map(Trackpoint::position),
        )
    }

    fun startTrackRecording() = tracks.startRecording()
    fun stopTrackRecording(): List<Trackpoint> = tracks.stopRecording()
    fun showGeometry(geometry: ElementGeometry) = content.showGeometry(geometry)
    fun setMarkers(markers: Iterable<Marker>) = content.setMarkers(markers)
    fun selectPins(icon: DrawableResource, positions: Collection<LatLon>) =
        content.selectPins(icon, positions)
    fun setPinMode(mode: MainMapPinMode) = content.updatePinMode(mode)
    fun hidePins() = content.hidePins()
    fun hideOverlay() = content.hideOverlay()
    fun clearSelectedPins() = content.clearSelectedPins()
    fun clearHighlighting() = content.clearHighlighting()

    internal fun onLocationChanged(location: Location?, track: List<LatLon>) =
        controller.onLocationChanged(location, track)

    internal fun onCameraChanged(
        position: CameraPosition,
        moveReason: CameraMoveReason,
        isMoving: Boolean,
    ) = controller.onCameraChanged(position, moveReason, isMoving)

    internal fun onMapPresented() = controller.onMapPresented()

    internal fun save() = controller.save()
}

/** Remembers the durable state and exact legacy camera policy used by [MainMap]. */
@Composable
fun rememberMainMapState(
    preferences: Preferences = koinInject(),
    runtime: MapRuntime = koinInject(),
): MainMapState {
    val persistedState = remember(preferences) { PreferencesMapCameraState(preferences) }
    val tracks = rememberSaveable(saver = MainMapTrackState.Saver) { MainMapTrackState() }
    val content = remember { MainMapContentState() }
    val colors = if (MaterialTheme.colors.isLight) MapColors.Light else MapColors.Night
    val languages = listOf(Locale.current.language)
    val styleConfiguration = remember { MainMapStyleConfiguration(colors, languages) }
    SideEffect {
        styleConfiguration.colors = colors
        styleConfiguration.languages = languages
    }
    val mapState = rememberStreetCompleteMapState(persistedState.loadCamera(), runtime) {
        MainMapStyle(mapState, styleConfiguration, tracks, content)
    }
    val scope = rememberCoroutineScope()
    val state = remember(mapState, persistedState, scope, tracks, content, styleConfiguration) {
        MainMapState(
            mapState,
            MainMapCameraController(
                camera = MapLibreCamera(mapState),
                persistedState = persistedState,
                scope = scope,
            ),
            tracks,
            content,
            styleConfiguration,
        )
    }
    DisposableEffect(state) {
        onDispose(state::save)
    }
    return state
}

internal interface MainMapCamera {
    val position: CameraPosition
    val visibleBoundingBox: BoundingBox?
    val viewportSize: DpSize?
    val isPresented: Boolean
    suspend fun animateTo(position: CameraPosition, duration: Duration)
}

private class MapLibreCamera(private val state: MapState) : MainMapCamera {
    override val position: CameraPosition get() = state.cameraPosition
    override val visibleBoundingBox: BoundingBox?
        get() = state.viewport?.visibleBoundingBox
    override val viewportSize: DpSize?
        get() = state.viewport?.size
    override val isPresented: Boolean get() = state.viewport != null

    override suspend fun animateTo(position: CameraPosition, duration: Duration) {
        state.animateCameraPosition(position, duration)
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
    var cameraPadding by mutableStateOf<PaddingValues>(PaddingValues(0.dp))
        private set

    private var track: List<LatLon> = emptyList()
    private var zoomedYet = false
    private var lastObservedPosition = camera.position
    private var previousFocusCamera: CameraPosition? = null
    private var pendingMove: PendingCameraMove? = null

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

    fun moveTo(
        position: LatLon,
        zoom: Double?,
        padding: PaddingValues,
        duration: Duration,
    ) {
        cameraPadding = padding
        val target = camera.position.copy(
            target = position.toPosition(),
            zoom = zoom ?: camera.position.zoom,
        )
        if (camera.isPresented) {
            scope.launch { camera.animateTo(target, duration) }
        } else {
            pendingMove = PendingCameraMove(target, duration)
        }
    }

    fun fitCluster(positions: List<LatLon>) {
        val target = clusterCameraPosition(
            current = camera.position,
            visibleBoundingBox = camera.visibleBoundingBox ?: return,
            positions = positions,
        ) ?: return
        val duration = max(450.0, abs(camera.position.zoom - target.zoom) * 450.0)
            .toLong()
            .milliseconds
        scope.launch { camera.animateTo(target, duration) }
    }

    fun startFocus(geometry: ElementGeometry, padding: PaddingValues) {
        val visibleBoundingBox = camera.visibleBoundingBox ?: return
        if (previousFocusCamera == null) previousFocusCamera = camera.position
        cameraPadding = padding

        val viewportSize = camera.viewportSize
        val horizontalFraction = viewportSize?.width?.value?.let { width ->
            if (width <= 0f) 1.0 else {
                val available = width - padding.calculateLeftPadding(LayoutDirection.Ltr).value -
                    padding.calculateRightPadding(LayoutDirection.Ltr).value
                (available / width).coerceIn(0.01f, 1f).toDouble()
            }
        } ?: 1.0
        val verticalFraction = viewportSize?.height?.value?.let { height ->
            if (height <= 0f) 1.0 else {
                val available = height - padding.calculateTopPadding().value -
                    padding.calculateBottomPadding().value
                (available / height).coerceIn(0.01f, 1f).toDouble()
            }
        } ?: 1.0

        val bounds = geometry.bounds
        val target = cameraPositionForBounds(
            current = camera.position,
            visibleBoundingBox = visibleBoundingBox,
            positions = listOf(bounds.min, bounds.max),
            zoomMargin = FOCUS_ZOOM_MARGIN,
            maxZoom = FOCUS_MAX_ZOOM,
            horizontalFraction = horizontalFraction,
            verticalFraction = verticalFraction,
        ) ?: return
        val zoomDifference = abs(camera.position.zoom - target.zoom)
        val finalTarget = if (zoomDifference > FOCUS_MIN_ZOOM_DIFFERENCE) {
            target
        } else {
            target.copy(zoom = camera.position.zoom)
        }
        val duration = max(450.0, zoomDifference * 450.0).toLong().milliseconds
        scope.launch { camera.animateTo(finalTarget, duration) }
    }

    fun clearFocus() {
        previousFocusCamera = null
        cameraPadding = PaddingValues(0.dp)
    }

    fun endFocus() {
        val previous = previousFocusCamera ?: return
        previousFocusCamera = null
        cameraPadding = PaddingValues(0.dp)
        val duration = max(300.0, abs(camera.position.zoom - previous.zoom) * 300.0)
            .toLong()
            .milliseconds
        scope.launch {
            camera.animateTo(
                camera.position.copy(target = previous.target, zoom = previous.zoom),
                duration,
            )
        }
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

    fun onLocationRestored(location: Location?, track: List<LatLon>) {
        displayedLocation = location
        this.track = track
    }

    fun onMapPresented() {
        val move = pendingMove
        if (move != null) {
            pendingMove = null
            scope.launch { camera.animateTo(move.position, move.duration) }
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
            // TODO(maplibre-compose): Replace camera-target delta inference when common callbacks
            // expose the pan-start screen coordinate and continuously updated gesture focal point.
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

private data class PendingCameraMove(
    val position: CameraPosition,
    val duration: Duration,
)

private fun Position.toLatLon() = LatLon(latitude = latitude, longitude = longitude)

/** Utility function to estimate current bearing from a track. */
internal fun getTrackBearingFromPositions(track: List<LatLon>): Double? {
    val last = track.lastOrNull() ?: return null
    val point = track.findLast { it.distanceTo(last) > MIN_TRACK_DISTANCE_FOR_BEARING } ?: return null
    return point.initialBearingTo(last)
}

private const val MIN_TRACK_DISTANCE_FOR_BEARING = 15f

/** Reproduces the legacy clustered-pin camera plan from the current rendered viewport. */
internal fun clusterCameraPosition(
    current: CameraPosition,
    visibleBoundingBox: BoundingBox,
    positions: List<LatLon>,
): CameraPosition? {
    return cameraPositionForBounds(
        current = current,
        visibleBoundingBox = visibleBoundingBox,
        positions = positions,
        zoomMargin = CLUSTER_ZOOM_MARGIN,
        maxZoom = CLUSTER_MAX_ZOOM,
    )
}

private fun cameraPositionForBounds(
    current: CameraPosition,
    visibleBoundingBox: BoundingBox,
    positions: List<LatLon>,
    zoomMargin: Double,
    maxZoom: Double,
    horizontalFraction: Double = 1.0,
    verticalFraction: Double = 1.0,
): CameraPosition? {
    if (positions.isEmpty()) return null

    val firstX = longitudeToMercatorX(positions.first().longitude)
    val unwrappedX = positions.map { position ->
        val x = longitudeToMercatorX(position.longitude)
        x + when {
            x - firstX > 0.5 -> -1.0
            x - firstX < -0.5 -> 1.0
            else -> 0.0
        }
    }
    val targetMinX = unwrappedX.min()
    val targetMaxX = unwrappedX.max()
    val targetY = positions.map { latitudeToMercatorY(it.latitude) }
    val targetMinY = targetY.min()
    val targetMaxY = targetY.max()

    val visibleWidth = longitudeSpan(
        visibleBoundingBox.southwest.longitude,
        visibleBoundingBox.northeast.longitude,
    )
    val visibleHeight = abs(
        latitudeToMercatorY(visibleBoundingBox.southwest.latitude) -
            latitudeToMercatorY(visibleBoundingBox.northeast.latitude)
    )
    val marginFactor = 2.0.pow(zoomMargin)
    val targetWidth = (targetMaxX - targetMinX) * marginFactor
    val targetHeight = (targetMaxY - targetMinY) * marginFactor
    val zoomCandidates = buildList {
        if (visibleWidth > 0.0 && targetWidth > 0.0) {
            add(current.zoom + log2(visibleWidth * horizontalFraction / targetWidth))
        }
        if (visibleHeight > 0.0 && targetHeight > 0.0) {
            add(current.zoom + log2(visibleHeight * verticalFraction / targetHeight))
        }
    }
    val targetZoom = (zoomCandidates.minOrNull() ?: maxZoom).coerceIn(0.0, maxZoom)

    val centerX = normalizeMercatorX((targetMinX + targetMaxX) / 2.0)
    val centerY = (targetMinY + targetMaxY) / 2.0
    return current.copy(
        target = Position(
            longitude = mercatorXToLongitude(centerX),
            latitude = mercatorYToLatitude(centerY),
        ),
        zoom = targetZoom,
    )
}

private fun longitudeToMercatorX(longitude: Double) = (longitude + 180.0) / 360.0
private fun mercatorXToLongitude(x: Double) = x * 360.0 - 180.0
private fun normalizeMercatorX(x: Double) = ((x % 1.0) + 1.0) % 1.0

private fun latitudeToMercatorY(latitude: Double): Double {
    val radians = latitude.coerceIn(-MAX_MERCATOR_LATITUDE, MAX_MERCATOR_LATITUDE) * PI / 180.0
    return (1.0 - ln(tan(radians) + 1.0 / kotlin.math.cos(radians)) / PI) / 2.0
}

private fun mercatorYToLatitude(y: Double): Double =
    (2.0 * atan(exp((1.0 - 2.0 * y) * PI)) - PI / 2.0) * 180.0 / PI

private fun longitudeSpan(west: Double, east: Double): Double {
    val raw = east - west
    if (abs(raw) >= 360.0) return 1.0
    return ((raw % 360.0) + 360.0) % 360.0 / 360.0
}

private const val CLUSTER_ZOOM_MARGIN = 0.25
private const val CLUSTER_MAX_ZOOM = 19.0
private const val FOCUS_ZOOM_MARGIN = 0.75
private const val FOCUS_MAX_ZOOM = 19.0
private const val FOCUS_MIN_ZOOM_DIFFERENCE = 0.5
private const val MAX_MERCATOR_LATITUDE = 85.0511287798066
private val MAP_CLICK_RADIUS = 14.dp
