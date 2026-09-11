package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.map.BASE_STYLE
import de.westnordost.streetcomplete.screens.main.map.MainMap
import de.westnordost.streetcomplete.screens.main.map.MainMapContent
import de.westnordost.streetcomplete.screens.main.map.MainMapViewModel
import de.westnordost.streetcomplete.screens.main.map.layers.Marker
import de.westnordost.streetcomplete.screens.main.map.toGeoJsonBoundingBox
import de.westnordost.streetcomplete.screens.main.map.toStreetCompleteBoundingBox
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionDropdownMenu
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration

@Composable
fun ShowMapScreen(
    onClickBack: () -> Unit,
    viewModel: MainMapViewModel = koinViewModel(),
    bottomSheetViewModel: MainBottomSheetViewModel = koinViewModel(),
    editHistoryViewModel: EditHistoryViewModel = koinViewModel(),
    overlayController: SelectedOverlayController = koinInject(),
    overlayRegistry: OverlayRegistry = koinInject(),
    runtime: MapRuntime = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val sheet by bottomSheetViewModel.shownBottomSheet.collectAsState()
    val edit by editHistoryViewModel.selectedEdit.collectAsState()
    var history by rememberSaveable { mutableStateOf(false) }
    val selectedOverlay by viewModel.selectedOverlay.collectAsState()
    val downloadedTiles by viewModel.downloadedTiles.collectAsState()
    var location by remember { mutableStateOf<Location?>(null) }
    var trackpoints by remember { mutableStateOf<List<LatLon>>(emptyList()) }
    var oldTrackpointsLists by remember { mutableStateOf<List<List<LatLon>>>(emptyList()) }
    var markers by remember { mutableStateOf<Collection<Marker>?>(null) }
    val recording = trackpoints.isNotEmpty()
    var overlayMenu by remember { mutableStateOf(false) }
    var lastEvent by remember { mutableStateOf("Tap a quest, edit, overlay element, or map background") }
    var styleRevision by remember { mutableStateOf(0) }

    val selectedEdit = if (history) edit else null
    val highlightedGeometry by produceState<ElementGeometry?>(null, selectedEdit, editHistoryViewModel) {
        value = selectedEdit?.let { editHistoryViewModel.getEditGeometry(it) }
    }

    val mapState = rememberMapState(
        runtime = runtime,
        baseStyle = BaseStyle.Json(BASE_STYLE.replace("\"Empty\"", "\"Debug $styleRevision\"")),
    ) {
        val state = checkNotNull(LocalMapState.current)
        val showPinsAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 13 } }
        val showOverlayAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 14 } }
        val showOverlay = selectedOverlay != null && sheet !is ShownBottomSheet.OsmQuest &&
            sheet !is ShownBottomSheet.OsmNoteQuest && !history
        val questPins = if (!history && sheet == null && showPinsAtZoom) {
            viewModel.questPins.collectAsState().value
        } else emptyList()
        val editHistoryPins = if (history && showPinsAtZoom) {
            viewModel.editHistoryPins.collectAsState().value
        } else emptyList()
        val styledElements = if (showOverlay && showOverlayAtZoom) {
            viewModel.styleableElements.collectAsState().value
        } else emptyList()
        MainMapContent(
            location = location,
            rotation = if (location != null) 135f else null,
            isRecording = recording,
            trackpoints = trackpoints,
            oldTrackpointsLists = oldTrackpointsLists,
            shownBottomSheet = sheet,
            shownMarkers = markers,
            isShowingUndoHistorySidebar = history,
            selectedOverlay = selectedOverlay,
            selectedEdit = selectedEdit,
            highlightedGeometry = highlightedGeometry,
            downloadedTiles = downloadedTiles,
            questPins = questPins,
            editHistoryPins = editHistoryPins,
            styledElements = styledElements,
            onClickElement = { properties ->
                val key = viewModel.getElementKey(properties)
                if (key == null) ClickResult.Pass else {
                    lastEvent = "Overlay: $key"
                    selectedOverlay?.let { bottomSheetViewModel.showElementInOverlay(it, key) }
                    ClickResult.Consume
                }
            },
            onClickQuest = { properties ->
                val key = viewModel.getQuestKey(properties)
                if (key == null) ClickResult.Pass else {
                    lastEvent = "Quest: $key"
                    bottomSheetViewModel.showQuest(key)
                    ClickResult.Consume
                }
            },
            onClickEdit = { properties ->
                val key = viewModel.getEditKey(properties)
                if (key == null) ClickResult.Pass else {
                    lastEvent = "Edit: $key"
                    editHistoryViewModel.select(key)
                    ClickResult.Consume
                }
            },
        )
    }
    // Apply the MapLibre viewport to StreetComplete's quest and overlay data sources.
    LaunchedEffect(mapState, viewModel) {
        snapshotFlow { mapState.cameraPosition.zoom to mapState.viewport?.visibleBounds }
            .collect { (zoom, bounds) -> viewModel.onViewportChanged(zoom, bounds?.toStreetCompleteBoundingBox()) }
    }

    fun clearSelection() {
        bottomSheetViewModel.closeBottomSheet()
        editHistoryViewModel.select(null)
        markers = null
    }

    fun cameraPosition(): LatLon = mapState.cameraPosition.target.toLatLon()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Show map") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(
                enabled = downloadedTiles.isNotEmpty(),
                onClick = {
                    val bounds = downloadedTiles.first().asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
                    mapState.setCameraPosition(mapState.cameraPosition.copy(
                        target = Position(
                            (bounds.min.longitude + bounds.max.longitude) / 2,
                            (bounds.min.latitude + bounds.max.latitude) / 2,
                        ),
                        zoom = 17.0,
                    ))
                }
            ) { Text("Downloaded area") }
            TextButton(onClick = {
                clearSelection()
                history = !history
            }) { Text(if (history) "Show quests" else "Show history") }
            Column {
                TextButton(onClick = { overlayMenu = true }) { Text("Overlay") }
                OverlaySelectionDropdownMenu(
                    expanded = overlayMenu,
                    onDismissRequest = { overlayMenu = false },
                    overlays = overlayRegistry.toList(),
                    onSelect = {
                        clearSelection()
                        history = false
                        overlayController.selectedOverlay = it
                    },
                )
            }
            TextButton(
                enabled = selectedOverlay?.isCreateNodeEnabled == true,
                onClick = { selectedOverlay?.let { bottomSheetViewModel.showCreateElementInOverlay(it) } },
            ) { Text("Create") }
            TextButton(onClick = ::clearSelection) { Text("Clear selection") }
            TextButton(
                enabled = sheet?.geometry != null || edit != null,
                onClick = {
                    scope.launch {
                        val geometry = highlightedGeometry ?: sheet?.geometry ?: return@launch
                        val camera = mapState.cameraPosition
                        mapState.animateCameraToBounds(
                            geometry.bounds.toGeoJsonBoundingBox(), camera.bearing, camera.tilt,
                        )
                    }
                }
            ) { Text("Focus selection") }
        }
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick = {
                location = if (location == null) {
                    Location(cameraPosition(), 20f, Duration.ZERO)
                } else null
            }) { Text(if (location == null) "Sample location" else "Hide location") }
            TextButton(onClick = {
                if (recording) {
                    trackpoints = emptyList()
                    oldTrackpointsLists = emptyList()
                } else {
                    val center = cameraPosition()
                    trackpoints = listOf(
                        LatLon(center.latitude - 0.0005, center.longitude - 0.0005),
                        LatLon(center.latitude, center.longitude - 0.0003),
                        center,
                    )
                    oldTrackpointsLists = listOf(listOf(
                        LatLon(center.latitude + 0.0005, center.longitude - 0.0005),
                        LatLon(center.latitude + 0.0005, center.longitude + 0.0005),
                    ))
                }
            }) { Text(if (recording) "Hide sample track" else "Sample track") }
            TextButton(onClick = {
                markers = if (markers == null) listOf(Marker(
                    geometry = sheet?.geometry ?: highlightedGeometry
                        ?: ElementPointGeometry(cameraPosition()),
                    icon = Res.drawable.preset_maki_circle,
                    title = "Sample marker",
                )) else null
            }) { Text(if (markers == null) "Sample markers" else "Hide markers") }
            TextButton(onClick = { styleRevision++ }) { Text("Reload style") }
        }
        Text(
            text = lastEvent,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        MainMap(
            state = mapState,
            onMapClick = { event ->
                lastEvent = "Map: ${event.position}"
                ClickResult.Consume
            },
            onMapLongClick = { event ->
                lastEvent = "Long press: ${event.position}"
                ClickResult.Consume
            },
            modifier = Modifier.weight(1f),
        )
    }
}
