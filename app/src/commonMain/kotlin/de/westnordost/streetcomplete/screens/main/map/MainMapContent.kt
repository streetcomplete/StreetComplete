package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.intl.LocaleList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.ShownEdit
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import de.westnordost.streetcomplete.screens.main.map.layers.getIcon
import de.westnordost.streetcomplete.screens.main.map.layers.toGeoJsonFeatures
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.units.International

/** Which pins the map shows */
enum class PinsMode { Quests, EditHistory, None }

/** Everything StreetComplete draws on the map. Quest pins, edit history pins and overlay elements
 *  are only collected from the [viewModel] while they are shown. */
@Composable
@MaplibreComposable
internal fun MainMapContent(
    viewModel: MainMapViewModel,
    location: LocationMeasurement?,
    /** compass heading in degrees, clockwise from north */
    heading: Float?,
    isRecording: Boolean,
    trackpoints: List<LatLon>,
    oldTrackpointsLists: List<List<LatLon>>,
    shownBottomSheet: ShownBottomSheet?,
    /** the edit selected in the edit history, highlighted on the map */
    selectedEdit: ShownEdit?,
    shownMarkers: Collection<Marker>?,
    /** labels of the background map to hide, e.g. because the selected overlay replaces them */
    hiddenLabels: Set<MapLabel>,
    /** whether the selected overlay's [styledElements] are displayed at all */
    showOverlay: Boolean,
    downloadedTiles: Collection<TilePos>,
    pinsMode: PinsMode,
    /** whether clicking pins and overlay elements selects them. Otherwise, the click falls
     *  through to the map */
    isSelectable: Boolean,
    onClickQuest: (QuestKey) -> Unit,
    onClickEdit: (EditKey) -> Unit,
    onClickElement: (ElementKey) -> Unit,
) {
    val mapState = checkNotNull(LocalMapState.current)
    val showPinsAtZoom by remember(mapState) { derivedStateOf { mapState.cameraPosition.zoom >= 13 } }
    val showOverlayAtZoom by remember(mapState) { derivedStateOf { mapState.cameraPosition.zoom >= 14 } }

    val pins: Collection<Pin> = if (!showPinsAtZoom) emptyList() else when (pinsMode) {
        PinsMode.Quests -> viewModel.questPins.collectAsStateWithLifecycle().value
        PinsMode.EditHistory -> viewModel.editHistoryPins.collectAsStateWithLifecycle().value
        PinsMode.None -> emptyList()
    }
    val styledElements: Collection<StyledElement> = if (showOverlay && showOverlayAtZoom) {
        viewModel.styleableElements.collectAsStateWithLifecycle().value
    } else emptyList()

    val scope = rememberCoroutineScope()
    fun <T : Any> select(key: T?, onSelect: (T) -> Unit): ClickResult =
        if (key == null || !isSelectable) ClickResult.Pass
        else { onSelect(key); ClickResult.Consume }
    val onClickPin: (JsonObject) -> ClickResult = when (pinsMode) {
        PinsMode.Quests -> { properties -> select(viewModel.getQuestKey(properties), onClickQuest) }
        PinsMode.EditHistory -> { properties -> select(viewModel.getEditKey(properties), onClickEdit) }
        PinsMode.None -> { _ -> ClickResult.Pass }
    }
    val onClickElementProperties: (JsonObject) -> ClickResult = { properties ->
        select(viewModel.getElementKey(properties), onClickElement)
    }

    val selectedQuest = when (shownBottomSheet) {
        is ShownBottomSheet.OsmNoteQuest -> shownBottomSheet.quest
        is ShownBottomSheet.OsmQuest -> shownBottomSheet.quest
        else -> null
    }
    val selectedOverlayElement = shownBottomSheet as? ShownBottomSheet.Overlay

    val languages = LocaleList.current.localeList.map { it.language }.distinct()
    val colors = if (MaterialTheme.colors.isLight) MapColors.Light else MapColors.Night

    val mapImages = rememberMapImages(mapState)
    val overlayIcons = remember(styledElements) {
        styledElements.mapNotNullTo(LinkedHashSet()) { it.style.getIcon() }.toList()
    }
    val overlayData by produceState<List<Feature<Geometry, JsonObject>>>(emptyList(), styledElements) {
        value = withContext(Dispatchers.Default) {
            styledElements.flatMap { it.toGeoJsonFeatures() }
        }
    }
    val overlaySource = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(overlayData)),
    )

    MapStyle(
        colors = colors,
        languages = languages,
        hiddenLabels = hiddenLabels,
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
                    onClickElement = onClickElementProperties
                )
            }
            TracksLayers(trackpoints, isRecording, oldTrackpointsLists)
        },
        aboveLabelsContent = {
            // these are always on top of everything else (including labels)
            if (showOverlay) {
                StyleableOverlayLabelLayer(
                    source = overlaySource,
                    icons = overlayIcons,
                    mapImages = mapImages,
                    color = colors.text,
                    haloColor = colors.textOutline,
                    onClickElement = onClickElementProperties
                )
            }
            shownMarkers?.let { markers ->
                GeometryMarkersLayers(
                    markers = markers,
                    haloColor = colors.textOutline,
                    mapImages = mapImages
                )
            }
            (shownBottomSheet?.geometry ?: selectedEdit?.geometry)?.let { geometry ->
                FocusedGeometryLayers(geometry)
            }

            location?.let {
                CurrentLocationLayers(
                    position = it.position.toLatLon(),
                    accuracy = it.horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
                    heading = heading,
                )
            }

            PinsLayers(
                pins = pins,
                mapImages = mapImages,
                onClickPin = onClickPin,
                onZoomToCluster = { zoom ->
                    scope.launch { mapState.animateCameraPosition(mapState.cameraPosition.copy(zoom = zoom)) }
                }
            )

            if (selectedEdit != null) {
                val icon = selectedEdit.edit.icon
                if (icon != null) {
                    SelectedPinsLayer(
                        icon = icon,
                        pinPositions = listOf(selectedEdit.edit.position)
                    )
                }
            } else if (selectedOverlayElement?.element != null) {
                val geometry = selectedOverlayElement.geometry
                if (geometry != null) {
                    SelectedPinsLayer(
                        icon = selectedOverlayElement.overlay.icon,
                        pinPositions = listOf(geometry.center)
                    )
                }
            } else if (selectedQuest != null) {
                SelectedPinsLayer(
                    icon = selectedQuest.type.icon,
                    pinPositions = selectedQuest.markerLocations
                )
            }
        }
    )
}

// need to refer to the local (font) resources platform-independently
internal val BASE_STYLE = """
    {
      "version": 8,
      "name": "Empty",
      "metadata": {},
      "sources": {},
      "glyphs": "${
        Res.getUri("files/glyphs/Roboto Regular/0-255.pbf")
            .replace("Roboto%20Regular", "{fontstack}")
            .replace("Roboto Regular", "{fontstack}")
            .replace("0-255", "{range}")
      }",
      "layers": []
    }
    """.trimIndent()
