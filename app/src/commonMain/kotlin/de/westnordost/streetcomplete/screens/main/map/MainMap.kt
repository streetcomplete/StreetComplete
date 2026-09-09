package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.map.MapUiOptions
import org.maplibre.compose.overlay.MapOverlay
import kotlin.time.Duration.Companion.milliseconds

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
    overlay: MapOverlay = MapOverlay {},
    viewModel: MainMapViewModel = koinViewModel(),
) {
    val mapState = state.mapState
    val lifecycleOwner = LocalLifecycleOwner.current
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
            mapState.cameraPosition to mapState.isCameraMoving
        }.collect { (position, isMoving) ->
            state.onCameraChanged(position, isMoving)
        }
    }
    LaunchedEffect(mapState, state) {
        snapshotFlow { mapState.viewport }.filterNotNull().first()
        state.onMapPresented()
    }
    LaunchedEffect(state, locationEvent) {
        locationEvent?.let(state::onLocationEvent)
    }
    LaunchedEffect(mapState, viewModel) {
        snapshotFlow {
            mapState.cameraPosition.zoom to mapState.viewport?.visibleBounds
        }.collect { (zoom, displayedArea) ->
            viewModel.onViewportChanged(
                zoom = zoom,
                displayedArea = displayedArea?.toStreetCompleteBoundingBox(),
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

    val currentOnClickMap = rememberUpdatedState(onClickMap)
    val currentOnLongPress = rememberUpdatedState(onLongPress)
    val interactions = remember(state) {
        MapInteractions {
            camera {
                pan {
                    onStart(state::onPanStarted)
                    momentum {
                        minimumSpeed = 250.0
                        baseTime = 500.milliseconds
                    }
                }
                zoom { onStart(state::onCameraInputStarted) }
                rotate { onStart(state::onCameraInputStarted) }
                tilt { onStart(state::onCameraInputStarted) }
            }
            callbacks {
                click {
                    onUnhandled unhandled@{ event ->
                        val position = event.position ?: return@unhandled ClickResult.Pass
                        val latLon = LatLon(position.latitude, position.longitude)
                        state.clickRadiusInMeters(latLon, event.screenOffset)?.let { radius ->
                            currentOnClickMap.value(latLon, radius)
                        }
                        ClickResult.Pass
                    }
                }
                longClick {
                    onEvent longClick@{ event ->
                        val position = event.position ?: return@longClick ClickResult.Pass
                        currentOnLongPress.value(
                            event.screenOffset,
                            LatLon(position.latitude, position.longitude),
                        )
                        ClickResult.Consume
                    }
                }
            }
        }
    }
    val uiOptions = remember {
        MapUiOptions {
            bindings {
                drag { pan { startSlop = 5.dp } }
                transform {
                    pan { startSlop = 5.dp }
                    rotate {
                        startAngle = 1.5
                        allowDuringZoom = false
                    }
                    tilt { startSlop = 8.dp }
                }
            }
        }
    }
    StreetCompleteMap(
        state = mapState,
        modifier = modifier,
        cameraPadding = state.cameraPadding,
        interactions = interactions,
        uiOptions = uiOptions,
        overlay = overlay,
    )
}

data class SelectedMapPins(
    val icon: DrawableResource,
    val positions: Collection<LatLon>,
)
