package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.Marker
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayer
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import de.westnordost.streetcomplete.screens.main.map.layers.toGeoJsonFeatures
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.StyleState
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MapClickHandler
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

/**
 * MapLibre Map with StreetComplete theme and all the StreetComplete specific things displayed on
 * top.
 *
 * @param shownBottomSheet the bottom sheet currently shown. Depending on which bottom sheet is
 * shown, certain elements will be highlighted or hidden.
 *
 * @param shownMarkers geometry markers currently shown, such as nearby shops when the user has a
 * quest form that asks something about shops open. This is usually tied to the quest, but the quest
 * form can (split way, level of place quest, …) change what markers are shown, so this is decoupled
 * from [shownBottomSheet]
 *
 * @param isShowingUndoHistorySidebar whether the undo history sidebar is open. The overlay is
 * hidden when it is open.
 * */
@Composable
fun MainMap(
    onClickOverlayElement: (ElementKey) -> Unit,
    onClickQuest: (QuestKey) -> Unit,
    onClickEdit: (EditKey) -> Unit,
    location: Location?,
    rotation: Float?,
    isRecording: Boolean,
    trackpoints: List<LatLon>,
    oldTrackpointsLists: List<List<LatLon>>,
    shownBottomSheet: ShownBottomSheet?,
    shownMarkers: Collection<Marker>?,
    isShowingUndoHistorySidebar: Boolean,
    modifier: Modifier = Modifier,
    onMapLongClick: MapClickHandler = { _, _ -> ClickResult.Pass },
    viewModel: MainMapViewModel = koinViewModel(),
    cameraState: CameraState = rememberCameraState(),
    styleState: StyleState = rememberStyleState(),
) {
    val coroutineScope = rememberCoroutineScope()

    val downloadedTiles by viewModel.downloadedTiles.collectAsState()
    val editHistoryPins by viewModel.editHistoryPins.collectAsState()
    val styledElements by viewModel.styleableElements.collectAsState()
    val questPins by viewModel.questPins.collectAsState()

    // because quests highlight additional information and history sidebar should feel clean
    val showOverlay = shownBottomSheet !is ShownBottomSheet.OsmQuest && !isShowingUndoHistorySidebar

    val selectedQuest = when (shownBottomSheet) {
        is ShownBottomSheet.OsmNoteQuest -> shownBottomSheet.quest
        is ShownBottomSheet.OsmQuest -> shownBottomSheet.quest
        else -> null
    }

    LaunchedEffect(cameraState.position) {
        viewModel.onMapMoved(cameraState)
    }

    fun zoomToCluster(targetZoom: Double) {
        coroutineScope.launch {
            cameraState.animateTo(cameraState.position.copy(zoom = targetZoom))
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Json(BASE_STYLE),
        zoomRange = 0f..22f,
        cameraState = cameraState,
        styleState = styleState,
        onMapLongClick = onMapLongClick,
        overlay = MapOverlay.None
    ) {
        val languages = listOf(Locale.current.language)
        val colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light

        val overlaySource = rememberGeoJsonSource(
            GeoJsonData.Features(FeatureCollection(styledElements.flatMap { it.toGeoJsonFeatures() })),
        )

        MapStyle(
            colors = colors,
            languages = languages,
            belowRoadsContent = {
                // left-and-right lines should be rendered behind the actual road
                if (showOverlay) {
                    StyleableOverlaySideLayer(
                        source = overlaySource,
                        isBridge = false
                    )
                }
            },
            belowRoadsOnBridgeContent = {
                // left-and-right lines should be rendered behind the actual bridge road
                if (showOverlay) {
                    StyleableOverlaySideLayer(
                        source = overlaySource,
                        isBridge = true
                    )
                }
            },
            belowLabelsContent = {
                // labels should be on top of other layers
                DownloadedAreaLayer(downloadedTiles)
                if (showOverlay) {
                    StyleableOverlayLayers(
                        source = overlaySource,
                        onClickElement = { properties ->
                            viewModel.getElementKey(properties)?.let { onClickOverlayElement(it) }
                        }
                    )
                }
                TracksLayers(trackpoints, isRecording, oldTrackpointsLists)
            },
            aboveLabelsContent = {
                // these are always on top of everything else (including labels)
                if (showOverlay) {
                    StyleableOverlayLabelLayer(
                        source = overlaySource,
                        color = colors.text,
                        haloColor = colors.textOutline,
                        onClickElement = { properties ->
                            viewModel.getElementKey(properties)?.let { onClickOverlayElement(it) }
                        }
                    )
                }
                shownMarkers?.let { markers ->
                    GeometryMarkersLayers(shownMarkers)
                }
                shownBottomSheet?.geometry?.let { geometry ->
                    FocusedGeometryLayers(geometry)
                }

                if (location != null) {
                    CurrentLocationLayers(location = location, rotation = rotation)
                }

                // normal quest pins are not shown while edit history sidebar is open
                if (isShowingUndoHistorySidebar) {
                    PinsLayers(
                        pins = editHistoryPins,
                        onClickPin = { properties ->
                            viewModel.getEditKey(properties)?.let { onClickEdit(it) }
                        },
                        onZoomToCluster = ::zoomToCluster
                    )
                } else {
                    PinsLayers(
                        pins = questPins,
                        onClickPin = { properties ->
                            viewModel.getQuestKey(properties)?.let { onClickQuest(it) }
                        },
                        onZoomToCluster = ::zoomToCluster
                    )
                }

                if (selectedQuest != null) {
                    SelectedPinsLayer(selectedQuest.type.icon, selectedQuest.markerLocations)
                }
            }
        )
    }
}

// need to refer to the local (font) resources platform-independently
private val BASE_STYLE = """
    {
      "version": 8,
      "name": "Empty",
      "metadata": {},
      "sources": {},
      "glyphs": "${
        Res.getUri("files/glyphs/Roboto Regular/0-255.pbf")
            .replace("Roboto Regular", "{fontstack}")
            .replace("0-255", "{range}")
            // workaround for https://github.com/maplibre/maplibre-native/issues/4498
            .replace("file:///android_asset/", "asset://")
      }",
      "layers": []
    }
    """.trimIndent()
