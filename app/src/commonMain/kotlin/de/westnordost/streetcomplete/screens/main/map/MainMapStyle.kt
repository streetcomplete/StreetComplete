package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayMainLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import de.westnordost.streetcomplete.screens.main.map.layers.rememberStyleableOverlaySource
import de.westnordost.streetcomplete.util.ktx.toLocation
import org.maplibre.compose.map.MapState
import org.maplibre.compose.util.MaplibreComposable

/** Inputs owned outside the state-owned MapLibre style composition. */
@Stable
internal class MainMapStyleConfiguration(
    colors: MapColors,
    languages: List<String>,
) {
    var colors by mutableStateOf(colors)
    var languages by mutableStateOf(languages)
    var hiddenBaseLayerIds by mutableStateOf<Set<String>>(emptySet())
    var downloadedTiles by mutableStateOf<List<TilePos>>(emptyList())
    var questPins by mutableStateOf(PinSnapshot.Empty)
    var editHistoryPins by mutableStateOf(PinSnapshot.Empty)
    var styledElements by mutableStateOf<List<StyledElement>>(emptyList())
    var locationRotation by mutableStateOf<Float?>(null)

    var onClickOverlayElement: (ElementKey) -> Unit = {}
    var questKeyForProperties: (Map<String, String>) -> QuestKey? = { null }
    var editKeyForProperties: (Map<String, String>) -> EditKey? = { null }
    var onClickQuest: (QuestKey) -> Unit = {}
    var onClickEdit: (EditKey) -> Unit = {}
    var onClickCluster: (List<LatLon>) -> Unit = {}
}

/** StreetComplete's complete state-owned style for the shared main map. */
@Composable
@MaplibreComposable
internal fun MainMapStyle(
    mapState: MapState,
    configuration: MainMapStyleConfiguration,
    tracks: MainMapTrackState,
    content: MainMapContentState,
) {
    // TODO(maplibre-compose): Restore StreetComplete's 300ms, system-scale-aware global style
    // transition when MapLibre Compose exposes style transition configuration in common code.
    val styleableOverlaySource = rememberStyleableOverlaySource(configuration.styledElements)
    MapStyle(
        colors = configuration.colors,
        languages = configuration.languages,
        hiddenBaseLayerIds = configuration.hiddenBaseLayerIds,
        belowRoadsContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = false,
                visible = content.showStyleableOverlay,
            )
        },
        belowRoadsOnBridgeContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = true,
                visible = content.showStyleableOverlay,
            )
        },
        belowLabelsContent = {
            DownloadedAreaLayer(configuration.downloadedTiles)
            StyleableOverlayMainLayers(
                source = styleableOverlaySource,
                visible = content.showStyleableOverlay,
                onClickElement = { configuration.onClickOverlayElement(it) },
            )
            TracksLayers(
                tracks.currentRenderedTrack,
                tracks.isRecording,
                tracks.oldRenderedTracks,
            )
        },
        aboveLabelsContent = {
            StyleableOverlayLabelLayer(
                source = styleableOverlaySource,
                styledElements = configuration.styledElements,
                visible = content.showStyleableOverlay,
                onClickElement = { configuration.onClickOverlayElement(it) },
            )
            if (content.markers.isNotEmpty()) GeometryMarkersLayers(content.markers)
            content.highlightedGeometry?.let { FocusedGeometryLayers(it) }
            tracks.displayedMeasurement?.toLocation()?.let {
                CurrentLocationLayers(it, configuration.locationRotation)
            }

            val pinSnapshot = when (content.pinMode) {
                MainMapPinMode.NONE -> PinSnapshot.Empty
                MainMapPinMode.QUESTS -> configuration.questPins
                MainMapPinMode.EDITS -> configuration.editHistoryPins
            }
            PinsLayers(
                mapState = mapState,
                snapshot = pinSnapshot,
                visible = content.showPins && content.pinMode != MainMapPinMode.NONE,
                onClickPin = { properties ->
                    when (content.pinMode) {
                        MainMapPinMode.NONE -> Unit
                        MainMapPinMode.QUESTS -> {
                            configuration.questKeyForProperties(properties)
                                ?.let(configuration.onClickQuest)
                        }
                        MainMapPinMode.EDITS -> {
                            configuration.editKeyForProperties(properties)
                                ?.let(configuration.onClickEdit)
                        }
                    }
                },
                onClickCluster = { configuration.onClickCluster(it) },
            )

            content.selectedPins?.let { SelectedPinsLayer(it.icon, it.positions) }
        },
    )
}
