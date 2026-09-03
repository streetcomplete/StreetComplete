package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MapClickHandler

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
    hiddenBaseLayerIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
    state: MainMapState = rememberMainMapState(),
    // TODO(maplibre-compose): Configure StreetComplete's exact pan/rotate/tilt/fling thresholds
    // and disable rotation while scaling when the common gesture API exposes those controls.
    onFrame: (framesPerSecond: Double) -> Unit = {},
    overlay: MapOverlay = MapOverlay {},
    viewModel: MainMapViewModel = koinViewModel(),
) {
    val mapState = state.mapState
    val coroutineScope = rememberCoroutineScope()
    val downloadedTiles by viewModel.downloadedTiles.collectAsState()
    val questPins by viewModel.questPins.collectAsState()
    val editHistoryPins by viewModel.editHistoryPins.collectAsState()
    val styledElements by viewModel.styleableElements.collectAsState()

    val cameraPosition = mapState.cameraPosition
    val viewport = mapState.viewport
    LaunchedEffect(
        cameraPosition,
        mapState.cameraMoveReason,
        mapState.isCameraMoving,
    ) {
        state.onCameraChanged(
            position = cameraPosition,
            moveReason = mapState.cameraMoveReason,
            isMoving = mapState.isCameraMoving,
        )
    }
    LaunchedEffect(viewport) {
        if (viewport != null) state.onMapPresented()
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

    SideEffect {
        state.styleConfiguration.hiddenBaseLayerIds = hiddenBaseLayerIds
        state.styleConfiguration.downloadedTiles = downloadedTiles
        state.styleConfiguration.questPins = questPins
        state.styleConfiguration.editHistoryPins = editHistoryPins
        state.styleConfiguration.styledElements = styledElements
        state.styleConfiguration.locationRotation = locationRotation
        state.styleConfiguration.onClickOverlayElement = onClickOverlayElement
        state.styleConfiguration.questKeyForProperties = viewModel::getQuestKey
        state.styleConfiguration.editKeyForProperties = viewModel::getEditKey
        state.styleConfiguration.onClickQuest = onClickQuest
        state.styleConfiguration.onClickEdit = onClickEdit
        state.styleConfiguration.onClickCluster = state::fitCluster
    }

    val onClick: MapClickHandler = { position, offset ->
        coroutineScope.launch {
            // TODO(maplibre-compose): Replace this pre-query when a raw-map callback runs only
            // after interactive layer handlers have declined the same click.
            val hitInteractiveFeature = mapState.queryRenderedFeatures(
                offset = offset,
                layerIds = MAIN_MAP_INTERACTIVE_LAYER_IDS,
            ).isNotEmpty()
            if (!hitInteractiveFeature) {
                val latLon = LatLon(position.latitude, position.longitude)
                state.clickRadiusInMeters(latLon, offset)?.let { radius ->
                    onClickMap(latLon, radius)
                }
            }
        }
        ClickResult.Pass
    }
    val onLongClick: MapClickHandler = { position, offset ->
        onLongPress(offset, LatLon(position.latitude, position.longitude))
        ClickResult.Consume
    }

    StreetCompleteMap(
        state = mapState,
        modifier = modifier,
        cameraPadding = state.cameraPadding,
        onClick = onClick,
        onLongClick = onLongClick,
        onFrame = onFrame,
        overlay = overlay,
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
