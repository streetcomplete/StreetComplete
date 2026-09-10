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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.map.BASE_STYLE
import de.westnordost.streetcomplete.screens.main.map.MainMap
import de.westnordost.streetcomplete.screens.main.map.MainMapViewModel
import de.westnordost.streetcomplete.screens.main.map.layers.Marker
import de.westnordost.streetcomplete.screens.main.map.toGeoJsonBoundingBox
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionDropdownMenu
import de.westnordost.streetcomplete.ui.common.BackIcon
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickResult
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
) {
    val scope = rememberCoroutineScope()
    val sheet by bottomSheetViewModel.shownBottomSheet.collectAsState()
    val edit by editHistoryViewModel.selectedEdit.collectAsState()
    val history by viewModel.isShowingUndoHistorySidebar.collectAsState()
    val selectedOverlay by viewModel.selectedOverlay.collectAsState()
    val downloadedTiles by viewModel.downloadedTiles.collectAsState()
    val location by viewModel.location.collectAsState()
    val recording by viewModel.isRecording.collectAsState()
    val markers by viewModel.shownMarkers.collectAsState()
    var overlayMenu by remember { mutableStateOf(false) }
    var lastEvent by remember { mutableStateOf("Tap a quest, edit, overlay element, or map background") }
    var styleRevision by remember { mutableStateOf(0) }

    LaunchedEffect(sheet) { viewModel.shownBottomSheet.value = sheet }
    LaunchedEffect(edit, history) {
        viewModel.selectedEdit.value = edit.takeIf { history }
        viewModel.highlightedGeometry.value = edit?.takeIf { history }
            ?.let { editHistoryViewModel.getEditGeometry(it) }
    }

    fun clearSelection() {
        bottomSheetViewModel.closeBottomSheet()
        editHistoryViewModel.select(null)
        viewModel.shownMarkers.value = null
    }

    fun cameraPosition(): LatLon = viewModel.mapState.cameraPosition.target.let {
        LatLon(it.latitude, it.longitude)
    }

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
                    viewModel.mapState.setCameraPosition(viewModel.mapState.cameraPosition.copy(
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
                viewModel.isShowingUndoHistorySidebar.value = !history
            }) { Text(if (history) "Show quests" else "Show history") }
            Column {
                TextButton(onClick = { overlayMenu = true }) { Text("Overlay") }
                OverlaySelectionDropdownMenu(
                    expanded = overlayMenu,
                    onDismissRequest = { overlayMenu = false },
                    overlays = overlayRegistry.toList(),
                    onSelect = {
                        clearSelection()
                        viewModel.isShowingUndoHistorySidebar.value = false
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
                        val geometry = viewModel.highlightedGeometry.value ?: sheet?.geometry ?: return@launch
                        val camera = viewModel.mapState.cameraPosition
                        viewModel.mapState.animateCameraToBounds(
                            geometry.bounds.toGeoJsonBoundingBox(), camera.bearing, camera.tilt,
                        )
                    }
                }
            ) { Text("Focus selection") }
        }
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick = {
                viewModel.location.value = if (location == null) {
                    Location(cameraPosition(), 20f, Duration.ZERO)
                } else null
                viewModel.rotation.value = if (location == null) 135f else null
            }) { Text(if (location == null) "Sample location" else "Hide location") }
            TextButton(onClick = {
                if (recording) {
                    viewModel.trackpoints.value = emptyList()
                    viewModel.oldTrackpointsLists.value = emptyList()
                } else {
                    val center = cameraPosition()
                    viewModel.trackpoints.value = listOf(
                        LatLon(center.latitude - 0.0005, center.longitude - 0.0005),
                        LatLon(center.latitude, center.longitude - 0.0003),
                        center,
                    )
                    viewModel.oldTrackpointsLists.value = listOf(listOf(
                        LatLon(center.latitude + 0.0005, center.longitude - 0.0005),
                        LatLon(center.latitude + 0.0005, center.longitude + 0.0005),
                    ))
                }
                viewModel.isRecording.value = !recording
            }) { Text(if (recording) "Hide sample track" else "Sample track") }
            TextButton(onClick = {
                viewModel.shownMarkers.value = if (markers == null) listOf(Marker(
                    geometry = sheet?.geometry ?: viewModel.highlightedGeometry.value
                        ?: ElementPointGeometry(cameraPosition()),
                    icon = Res.drawable.preset_maki_circle,
                    title = "Sample marker",
                )) else null
            }) { Text(if (markers == null) "Sample markers" else "Hide markers") }
            TextButton(onClick = {
                styleRevision++
                checkNotNull(viewModel.mapState.style.asMutable).baseStyle = BaseStyle.Json(
                    BASE_STYLE.replace("\"Empty\"", "\"Debug $styleRevision\"")
                )
            }) { Text("Reload style") }
        }
        Text(
            text = lastEvent,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        MainMap(
            viewModel = viewModel,
            onClickOverlayElement = { key ->
                lastEvent = "Overlay: $key"
                selectedOverlay?.let { bottomSheetViewModel.showElementInOverlay(it, key) }
            },
            onClickQuest = { key ->
                lastEvent = "Quest: $key"
                bottomSheetViewModel.showQuest(key)
            },
            onClickEdit = { key ->
                lastEvent = "Edit: $key"
                editHistoryViewModel.select(key)
            },
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
