package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import kotlin.uuid.Uuid

@Composable
@MaplibreComposable
internal fun MainMapContent(
    location: Location?,
    rotation: Float?,
    isRecording: Boolean,
    trackpoints: List<LatLon>,
    oldTrackpointsLists: List<List<LatLon>>,
    shownBottomSheet: ShownBottomSheet?,
    shownMarkers: Collection<Marker>?,
    isShowingUndoHistorySidebar: Boolean,
    selectedOverlay: Overlay?,
    selectedEdit: Edit?,
    highlightedGeometry: ElementGeometry?,
    downloadedTiles: Collection<TilePos>,
    questPins: Collection<Pin>,
    editHistoryPins: Collection<Pin>,
    styledElements: Collection<StyledElement>,
    onClickQuest: (JsonObject) -> ClickResult,
    onClickEdit: (JsonObject) -> ClickResult,
    onClickElement: (JsonObject) -> ClickResult,
) {
    // because quests highlight additional information and history sidebar should feel clean
    val showOverlay = selectedOverlay != null && shownBottomSheet !is ShownBottomSheet.OsmQuest &&
        shownBottomSheet !is ShownBottomSheet.OsmNoteQuest && !isShowingUndoHistorySidebar

    val selectedQuest = when (val sheet = shownBottomSheet) {
        is ShownBottomSheet.OsmNoteQuest -> sheet.quest
        is ShownBottomSheet.OsmQuest -> sheet.quest
        else -> null
    }

    val selectedOverlayElement = shownBottomSheet as? ShownBottomSheet.Overlay
    val showQuestPins = !isShowingUndoHistorySidebar && shownBottomSheet == null

    val languages = listOf(Locale.current.language)
    val colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light

    val overlayData by produceState<List<Feature<Geometry, JsonObject>>>(emptyList(), styledElements) {
        value = withContext(Dispatchers.Default) {
            styledElements.flatMap { it.toGeoJsonFeatures() }
        }
    }
    val overlaySource = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(overlayData)),
    )

    // TODO maplibre-compose: Reuse layer IDs after https://github.com/maplibre/maplibre-native-ffi/issues/709.
    val layerIdSuffix = remember(checkNotNull(LocalMapState.current).style.baseStyle) { Uuid.random().toString() }
    MapStyle(
        layerIdSuffix = layerIdSuffix,
        colors = colors,
        languages = languages,
        hiddenLayers = selectedOverlay?.hidesLayers.orEmpty(),
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
                    onClickElement = onClickElement
                )
            }
            TracksLayers(trackpoints, isRecording, oldTrackpointsLists)
        },
        aboveLabelsContent = {
            // these are always on top of everything else (including labels)
            if (showOverlay) {
                StyleableOverlayLabelLayer(
                    source = overlaySource,
                    icons = styledElements.mapNotNull { it.style.getIcon() },
                    color = colors.text,
                    haloColor = colors.textOutline,
                    onClickElement = onClickElement
                )
            }
            shownMarkers?.let { markers ->
                GeometryMarkersLayers(markers, haloColor = colors.textOutline)
            }
            (highlightedGeometry ?: shownBottomSheet?.geometry)?.let { geometry ->
                FocusedGeometryLayers(geometry)
            }

            location?.let { CurrentLocationLayers(location = it, rotation = rotation) }

            // normal quest pins are not shown while edit history sidebar is open
            if (isShowingUndoHistorySidebar) {
                PinsLayers(
                    pins = editHistoryPins,
                    onClickPin = onClickEdit
                )
            } else if (showQuestPins) {
                PinsLayers(
                    pins = questPins,
                    onClickPin = onClickQuest
                )
            }

            val edit = selectedEdit
            if (isShowingUndoHistorySidebar && edit != null) {
                edit.icon?.let { icon -> SelectedPinsLayer(icon, listOf(edit.position)) }
            } else if (selectedOverlayElement?.element != null) {
                selectedOverlayElement.geometry?.let { geometry ->
                    SelectedPinsLayer(selectedOverlayElement.overlay.icon, listOf(geometry.center))
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
