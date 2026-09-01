package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayMainLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayers
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import de.westnordost.streetcomplete.screens.main.map.layers.rememberStyleableOverlaySource
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.map.MapPresentationCallbacks
import org.maplibre.compose.map.MapPresentationDetachedException
import org.maplibre.compose.map.MapPresentationOptions
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.util.ClickResult

/** Complete shared MapLibre Compose renderer for StreetComplete's main map. */
@Composable
fun MainMap(
    onClickOverlayElement: (ElementKey) -> Unit,
    onClickQuest: (QuestKey) -> Unit,
    onClickEdit: (de.westnordost.streetcomplete.data.edithistory.EditKey) -> Unit,
    onClickMap: (position: LatLon, clickRadiusInMeters: Double) -> Unit,
    onLongPress: (offset: DpOffset, position: LatLon) -> Unit,
    locationEvent: LocationEvent?,
    locationRotation: Float?,
    modifier: Modifier = Modifier,
    state: MainMapState = rememberMainMapState(),
    // TODO(maplibre-compose): Configure StreetComplete's exact pan/rotate/tilt/fling thresholds
    // and disable rotation while scaling when the common gesture API exposes those controls.
    presentationOptions: MapPresentationOptions = MapPresentationOptions(zoomRange = 0f..22f),
    onFrame: (framesPerSecond: Double) -> Unit = {},
    overlay: MapOverlay = MapOverlay.None,
    viewModel: MainMapViewModel = koinViewModel(),
) {
    val mapState = state.mapState
    val coroutineScope = rememberCoroutineScope()
    val downloadedTiles by viewModel.downloadedTiles.collectAsState()
    val questPins by viewModel.questPins.collectAsState()
    val editHistoryPins by viewModel.editHistoryPins.collectAsState()
    val styledElements by viewModel.styleableElements.collectAsState()

    val cameraPosition = mapState.cameraPosition
    val presentation = mapState.presentation
    val viewport = presentation?.viewport
    LaunchedEffect(
        cameraPosition,
        presentation?.cameraMoveReason,
        presentation?.isCameraMoving,
    ) {
        state.onCameraChanged(
            position = cameraPosition,
            moveReason = presentation?.cameraMoveReason
                ?: org.maplibre.compose.camera.CameraMoveReason.NONE,
            isMoving = presentation?.isCameraMoving == true,
        )
    }
    LaunchedEffect(presentation) {
        if (presentation != null) state.onMapPresented()
    }
    LaunchedEffect(locationEvent) {
        locationEvent?.let(state::onLocationEvent)
    }
    LaunchedEffect(cameraPosition.zoom, viewport?.visibleBoundingBox) {
        viewModel.onViewportChanged(
            zoom = cameraPosition.zoom,
            displayedArea = viewport?.visibleBoundingBox?.toStreetCompleteBoundingBox(),
        )
    }

    val styleableOverlaySource = rememberStyleableOverlaySource(mapState, styledElements)
    val callbacks = MapPresentationCallbacks(
        onClick = { position, offset ->
            val presentationAtClick = mapState.presentation
            coroutineScope.launch {
                val hitInteractiveFeature = try {
                    presentationAtClick?.queryRenderedFeatures(
                        offset = offset,
                        layerIds = MAIN_MAP_INTERACTIVE_LAYER_IDS,
                    )?.isNotEmpty() == true
                } catch (_: MapPresentationDetachedException) {
                    return@launch
                }
                if (!hitInteractiveFeature) {
                    val latLon = LatLon(position.latitude, position.longitude)
                    state.clickRadiusInMeters(latLon, offset)?.let { radius ->
                        onClickMap(latLon, radius)
                    }
                }
            }
            ClickResult.Pass
        },
        onLongClick = { position, offset ->
            onLongPress(offset, LatLon(position.latitude, position.longitude))
            ClickResult.Consume
        },
        onFrame = onFrame,
    )

    StreetCompleteMap(
        state = mapState,
        modifier = modifier,
        presentationOptions = presentationOptions.copy(cameraPadding = state.cameraPadding),
        callbacks = callbacks,
        overlay = overlay,
        belowRoadsContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = false,
                visible = state.showStyleableOverlay,
            )
        },
        belowRoadsOnBridgeContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = true,
                visible = state.showStyleableOverlay,
            )
        },
        belowLabelsContent = {
            DownloadedAreaLayer(downloadedTiles)
            StyleableOverlayMainLayers(
                source = styleableOverlaySource,
                visible = state.showStyleableOverlay,
                onClickElement = onClickOverlayElement,
            )
            TracksLayers(
                state.currentRenderedTrack,
                state.isRecordingTrack,
                state.oldRenderedTracks,
            )
        },
        aboveLabelsContent = {
            StyleableOverlayLabelLayer(
                source = styleableOverlaySource,
                styledElements = styledElements,
                visible = state.showStyleableOverlay,
                onClickElement = onClickOverlayElement,
            )
            if (state.markers.isNotEmpty()) GeometryMarkersLayers(state.markers)
            state.highlightedGeometry?.let { FocusedGeometryLayers(it) }
            state.displayedMeasurement?.toLocation()?.let {
                CurrentLocationLayers(it, locationRotation)
            }

            if (state.pinMode == MainMapPinMode.EDITS) {
                PinsLayers(
                    mapState = mapState,
                    pins = editHistoryPins,
                    visible = state.showPins,
                    onClickPin = { properties ->
                        viewModel.getEditKey(properties)?.let(onClickEdit)
                    },
                    onClickCluster = state::fitCluster,
                )
            } else if (state.pinMode == MainMapPinMode.QUESTS) {
                PinsLayers(
                    mapState = mapState,
                    pins = questPins,
                    visible = state.showPins,
                    onClickPin = { properties ->
                        viewModel.getQuestKey(properties)?.let(onClickQuest)
                    },
                    onClickCluster = state::fitCluster,
                )
            }

            state.selectedPins?.let { SelectedPinsLayer(it.icon, it.positions) }
        },
    )
}

data class SelectedMapPins(
    val icon: DrawableResource,
    val positions: Collection<LatLon>,
)

private val MAIN_MAP_INTERACTIVE_LAYER_IDS = setOf(
    "pin-cluster-layer",
    "pins-layer",
    "overlay-fills",
    "overlay-lines",
    "overlay-lines-dashed",
    "overlay-fills-outline",
    "overlay-symbols",
)
