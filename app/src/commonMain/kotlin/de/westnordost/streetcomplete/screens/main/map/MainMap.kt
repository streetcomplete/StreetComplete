package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    overlay: MapOverlay = MapOverlay {},
    viewModel: MainMapViewModel = koinViewModel(),
) {
    val mapState = state.mapState
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    // Materialize each value during composition. A local delegated property referenced only from
    // SideEffect defers its State.value read until the effect runs, so Compose would not observe
    // the flow and its update could remain invisible until an unrelated recomposition.
    val downloadedTiles = viewModel.downloadedTiles.collectAsState().value
    val questPins = viewModel.questPins.collectAsState().value
    val editHistoryPins = viewModel.editHistoryPins.collectAsState().value
    val styledElements = viewModel.styleableElements.collectAsState().value

    // Visibility is transient selection UI. Keep the active pin pipeline and its viewport cache
    // alive while the ordinary pins are hidden, matching master's layer-visibility toggle.
    val activePinMode = state.pinMode
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setPresented(true)
                Lifecycle.Event.ON_STOP -> viewModel.setPresented(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        viewModel.setPresented(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setPresented(false)
        }
    }
    LaunchedEffect(viewModel, activePinMode) {
        viewModel.setActivePinMode(activePinMode)
    }
    LaunchedEffect(mapState, state) {
        snapshotFlow {
            Triple(mapState.cameraPosition, mapState.cameraMoveReason, mapState.isCameraMoving)
        }.distinctUntilChanged().collect { (position, moveReason, isMoving) ->
            state.onCameraChanged(position, moveReason, isMoving)
        }
    }
    LaunchedEffect(mapState, state) {
        snapshotFlow { mapState.viewport }.filterNotNull().first()
        state.onMapPresented()
    }
    LaunchedEffect(locationEvent) {
        locationEvent?.let(state::onLocationEvent)
    }
    LaunchedEffect(mapState, viewModel) {
        snapshotFlow {
            mapState.cameraPosition.zoom to mapState.viewport?.visibleBoundingBox
        }.distinctUntilChanged().collect { (zoom, displayedArea) ->
            viewModel.onViewportChanged(
                zoom = zoom,
                displayedArea = displayedArea?.toBoundingBox(),
            )
        }
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
