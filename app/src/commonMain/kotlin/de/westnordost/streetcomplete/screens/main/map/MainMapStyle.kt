package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.MotionDurationScale
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.BindDynamicStyleImages
import de.westnordost.streetcomplete.screens.main.map.layers.DynamicStyleImageRegistry
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.ImperativeLayerVisibility
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayMainLayers
import de.westnordost.streetcomplete.screens.main.map.layers.STYLEABLE_OVERLAY_LAYER_IDS
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import de.westnordost.streetcomplete.screens.main.map.layers.isStyleHandleRace
import de.westnordost.streetcomplete.screens.main.map.layers.rememberStyleableOverlaySource
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.style.TransitionOptions
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
    val dynamicStyleImages = DynamicStyleImageRegistry()

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
    BindMainMapStyleTransition(mapState)
    BindDynamicStyleImages(mapState, configuration.dynamicStyleImages)
    val styleableOverlaySource =
        rememberStyleableOverlaySource(
            mapState,
            configuration.styledElements,
            configuration.dynamicStyleImages,
        )
    ImperativeLayerVisibility(
        mapState,
        STYLEABLE_OVERLAY_LAYER_IDS,
        content.showStyleableOverlay,
    )
    ImperativeLayerVisibility(
        mapState,
        listOf(HOUSE_NUMBER_LABEL_LAYER_ID),
        HOUSE_NUMBER_LABEL_LAYER_ID !in configuration.hiddenBaseLayerIds,
    )
    MapStyle(
        colors = configuration.colors,
        languages = configuration.languages,
        belowRoadsContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = false,
            )
        },
        belowRoadsOnBridgeContent = {
            StyleableOverlaySideLayers(
                source = styleableOverlaySource,
                bridge = true,
            )
        },
        belowLabelsContent = {
            DownloadedAreaLayer(mapState, configuration.downloadedTiles)
            StyleableOverlayMainLayers(
                source = styleableOverlaySource,
                onClickElement = { configuration.onClickOverlayElement(it) },
            )
            TracksLayers(
                mapState,
                tracks.currentRenderedTrack,
                tracks.isRecording,
                tracks.oldRenderedTracks,
            )
        },
        aboveLabelsContent = {
            StyleableOverlayLabelLayer(
                source = styleableOverlaySource,
                onClickElement = { configuration.onClickOverlayElement(it) },
            )
            // Keep the marker source and layers installed, matching master's component lifetime.
            GeometryMarkersLayers(
                mapState,
                content.markers,
                configuration.dynamicStyleImages,
            )
            // Keep the source and layers installed. The breathing animation updates one feature-
            // state value per frame without recomposing or replacing style resources.
            FocusedGeometryLayers(mapState, content.highlightedGeometry)
            CurrentLocationLayers(
                mapState,
                tracks.displayedMeasurement?.toLocation(),
                configuration.locationRotation,
            )

            val pinSnapshot = when (content.pinMode) {
                MainMapPinMode.NONE -> PinSnapshot.Empty
                MainMapPinMode.QUESTS -> configuration.questPins
                MainMapPinMode.EDITS -> configuration.editHistoryPins
            }
            PinsLayers(
                mapState = mapState,
                snapshot = pinSnapshot,
                visible = content.showPins,
                imageRegistry = configuration.dynamicStyleImages,
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

            // Keep the selected-pin source and layer installed, as master does. Selection changes
            // update their data and icon size imperatively without restructuring the style.
            SelectedPinsLayer(mapState, content.selectedPins, configuration.dynamicStyleImages)
        },
    )
}

@Composable
private fun BindMainMapStyleTransition(mapState: MapState) {
    LaunchedEffect(mapState) {
        val motionDurationScale = coroutineContext[MotionDurationScale]
        snapshotFlow {
            mapState.style.loadState to (motionDurationScale?.scaleFactor ?: 1f)
        }
            .distinctUntilChanged()
            .collectLatest { (loadState, durationScale) ->
                if (loadState != StyleLoadState.Ready) return@collectLatest
                val transition = mainMapStyleTransition(durationScale)
                try {
                    withContext(Dispatchers.Default) {
                        mapState.style.transition.set(transition)
                        mapState.style.transition.setPlacementTransitions(true)
                    }
                } catch (error: IllegalStateException) {
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }
}

internal fun mainMapStyleTransition(durationScale: Float): TransitionOptions =
    TransitionOptions(duration = (MAIN_MAP_STYLE_TRANSITION_MILLIS * durationScale).milliseconds)

private const val MAIN_MAP_STYLE_TRANSITION_MILLIS = 300.0

private const val HOUSE_NUMBER_LABEL_LAYER_ID = "labels-housenumbers"
