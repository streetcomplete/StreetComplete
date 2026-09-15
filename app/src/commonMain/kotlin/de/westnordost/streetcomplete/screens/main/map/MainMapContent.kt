package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
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
import de.westnordost.streetcomplete.util.ktx.toLatLon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.units.International

@Composable
@MaplibreComposable
internal fun MainMapContent(
    location: LocationMeasurement?,
    /** compass heading in degrees, clockwise from north */
    heading: Float?,
    isRecording: Boolean,
    trackpoints: List<LatLon>,
    oldTrackpointsLists: List<List<LatLon>>,
    shownBottomSheet: ShownBottomSheet?,
    shownMarkers: Collection<Marker>?,
    /** labels of the background map to hide, e.g. because the selected overlay replaces them */
    hiddenLabels: Set<MapLabel>,
    /** whether the selected overlay's [styledElements] are displayed at all */
    showOverlay: Boolean,
    selectedEdit: Edit?,
    highlightedGeometry: ElementGeometry?,
    downloadedTiles: Collection<TilePos>,
    pins: Collection<Pin>,
    onClickPin: (JsonObject) -> ClickResult,
    onClickCluster: (BoundingBox) -> Unit,
    styledElements: Collection<StyledElement>,
    onClickElement: (JsonObject) -> ClickResult,
) {
    val selectedQuest = when (val sheet = shownBottomSheet) {
        is ShownBottomSheet.OsmNoteQuest -> sheet.quest
        is ShownBottomSheet.OsmQuest -> sheet.quest
        else -> null
    }
    val selectedOverlayElement = shownBottomSheet as? ShownBottomSheet.Overlay

    val languages = listOf(Locale.current.language)
    val colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light

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
                    icons = overlayIcons,
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

            location?.let {
                CurrentLocationLayers(
                    position = it.position.toLatLon(),
                    accuracy = it.horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
                    heading = heading,
                )
            }

            PinsLayers(pins = pins, onClickPin = onClickPin, onClickCluster = onClickCluster)

            val edit = selectedEdit
            if (edit != null) {
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
