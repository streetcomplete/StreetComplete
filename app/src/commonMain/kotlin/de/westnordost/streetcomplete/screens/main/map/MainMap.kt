package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
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
import de.westnordost.streetcomplete.ui.common.quest.Marker
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.map.MapPresentationCallbacks
import org.maplibre.compose.map.MapPresentationOptions
import org.maplibre.compose.overlay.MapOverlay

/** Complete shared MapLibre Compose renderer for StreetComplete's main map. */
@Composable
fun MainMap(
    onClickOverlayElement: (ElementKey) -> Unit,
    onClickQuest: (QuestKey) -> Unit,
    onClickEdit: (de.westnordost.streetcomplete.data.edithistory.EditKey) -> Unit,
    location: Location?,
    locationRotation: Float?,
    trackpoints: List<LatLon>,
    isRecordingTrack: Boolean,
    oldTrackpointLists: List<List<LatLon>>,
    focusedGeometry: ElementGeometry?,
    shownMarkers: Collection<Marker>,
    selectedPins: SelectedMapPins?,
    isShowingEditHistory: Boolean,
    showStyleableOverlay: Boolean,
    modifier: Modifier = Modifier,
    state: MainMapState = rememberMainMapState(),
    // TODO(maplibre-compose): Configure StreetComplete's exact pan/rotate/tilt/fling thresholds
    // and disable rotation while scaling when the common gesture API exposes those controls.
    presentationOptions: MapPresentationOptions = MapPresentationOptions(zoomRange = 0f..22f),
    callbacks: MapPresentationCallbacks = MapPresentationCallbacks(),
    overlay: MapOverlay = MapOverlay.None,
    viewModel: MainMapViewModel = koinViewModel(),
) {
    val mapState = state.mapState
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
    LaunchedEffect(location, trackpoints) {
        state.onLocationChanged(location, trackpoints)
    }
    LaunchedEffect(cameraPosition.zoom, viewport?.visibleBoundingBox) {
        viewModel.onViewportChanged(
            zoom = cameraPosition.zoom,
            displayedArea = viewport?.visibleBoundingBox?.toStreetCompleteBoundingBox(),
        )
    }

    val styleableOverlaySource = rememberStyleableOverlaySource(mapState, styledElements)

    StreetCompleteMap(
        state = mapState,
        modifier = modifier,
        presentationOptions = presentationOptions,
        callbacks = callbacks,
        overlay = overlay,
        belowRoadsContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = false,
                visible = showStyleableOverlay,
            )
        },
        belowRoadsOnBridgeContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = true,
                visible = showStyleableOverlay,
            )
        },
        belowLabelsContent = {
            DownloadedAreaLayer(downloadedTiles)
            StyleableOverlayMainLayers(
                source = styleableOverlaySource,
                visible = showStyleableOverlay,
                onClickElement = onClickOverlayElement,
            )
            TracksLayers(trackpoints, isRecordingTrack, oldTrackpointLists)
        },
        aboveLabelsContent = {
            StyleableOverlayLabelLayer(
                source = styleableOverlaySource,
                styledElements = styledElements,
                visible = showStyleableOverlay,
                onClickElement = onClickOverlayElement,
            )
            if (shownMarkers.isNotEmpty()) GeometryMarkersLayers(shownMarkers)
            focusedGeometry?.let { FocusedGeometryLayers(it) }
            location?.let { CurrentLocationLayers(it, locationRotation) }

            if (isShowingEditHistory) {
                PinsLayers(
                    mapState = mapState,
                    pins = editHistoryPins,
                    onClickPin = { properties ->
                        viewModel.getEditKey(properties)?.let(onClickEdit)
                    },
                    onClickCluster = state::fitCluster,
                )
            } else {
                PinsLayers(
                    mapState = mapState,
                    pins = questPins,
                    onClickPin = { properties ->
                        viewModel.getQuestKey(properties)?.let(onClickQuest)
                    },
                    onClickCluster = state::fitCluster,
                )
            }

            selectedPins?.let { SelectedPinsLayer(it.icon, it.positions) }
        },
    )
}

data class SelectedMapPins(
    val icon: DrawableResource,
    val positions: Collection<LatLon>,
)
