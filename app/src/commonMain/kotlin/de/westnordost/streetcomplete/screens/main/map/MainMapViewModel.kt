package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import de.westnordost.streetcomplete.screens.main.map.layers.Marker
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.sources.EditHistoryPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.MapQuestPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.StyleableOverlaySource
import de.westnordost.streetcomplete.util.serialization.CameraPositionSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapState
import org.maplibre.compose.style.BaseStyle

abstract class MainMapViewModel : ViewModel() {
    abstract val mapState: MapState

    abstract val location: MutableStateFlow<Location?>
    abstract val rotation: MutableStateFlow<Float?>
    abstract val isRecording: MutableStateFlow<Boolean>
    abstract val trackpoints: MutableStateFlow<List<LatLon>>
    abstract val oldTrackpointsLists: MutableStateFlow<List<List<LatLon>>>
    abstract val shownBottomSheet: MutableStateFlow<ShownBottomSheet?>
    abstract val shownMarkers: MutableStateFlow<Collection<Marker>?>
    abstract val isShowingUndoHistorySidebar: MutableStateFlow<Boolean>

    abstract val selectedEdit: MutableStateFlow<Edit?>
    abstract val highlightedGeometry: MutableStateFlow<ElementGeometry?>
    abstract val selectedOverlay: StateFlow<Overlay?>

    abstract val clicks: SharedFlow<MainMapClick>

    /** Downloaded areas */
    abstract val downloadedTiles: StateFlow<Collection<TilePos>>

    /** Quest pins in current view */
    abstract val questPins: StateFlow<Collection<Pin>>
    abstract fun onClickQuest(properties: JsonObject): ClickResult

    /** Edit history pins in current view */
    abstract val editHistoryPins: StateFlow<Collection<Pin>>
    abstract fun onClickEdit(properties: JsonObject): ClickResult

    /** Styled elements (of overlay) in current view */
    abstract val styleableElements: StateFlow<Collection<StyledElement>>
    abstract fun onClickElement(properties: JsonObject): ClickResult
}

sealed interface MainMapClick {
    data class Quest(val key: QuestKey) : MainMapClick
    data class Edit(val key: EditKey) : MainMapClick
    data class Element(val key: ElementKey) : MainMapClick
}

class MainMapViewModelImpl(
    runtime: MapRuntime,
    savedStateHandle: SavedStateHandle,
    private val downloadedTilesSource: DownloadedTilesSource,
    private val mapQuestPinsSource: MapQuestPinsSource,
    private val editHistoryPinsSource: EditHistoryPinsSource,
    private val styleableOverlaySource: StyleableOverlaySource,
    private val selectedOverlaySource: SelectedOverlaySource,
) : MainMapViewModel() {

    override val location = MutableStateFlow<Location?>(null)
    override val rotation = MutableStateFlow<Float?>(null)
    override val isRecording = MutableStateFlow(false)
    override val trackpoints = MutableStateFlow<List<LatLon>>(emptyList())
    override val oldTrackpointsLists = MutableStateFlow<List<List<LatLon>>>(emptyList())
    override val shownBottomSheet = MutableStateFlow<ShownBottomSheet?>(null)
    override val shownMarkers = MutableStateFlow<Collection<Marker>?>(null)
    override val isShowingUndoHistorySidebar = MutableStateFlow(false)

    override val selectedEdit = MutableStateFlow<Edit?>(null)
    override val highlightedGeometry = MutableStateFlow<ElementGeometry?>(null)

    override val selectedOverlay = callbackFlow {
        val listener = object : SelectedOverlaySource.Listener {
            override fun onSelectedOverlayChanged() { trySend(selectedOverlaySource.selectedOverlay) }
        }
        selectedOverlaySource.addListener(listener)
        trySend(selectedOverlaySource.selectedOverlay)
        awaitClose { selectedOverlaySource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), null)

    private val _clicks = MutableSharedFlow<MainMapClick>()
    override val clicks = _clicks.asSharedFlow()

    private var savedCameraPosition by savedStateHandle.saved(CameraPositionSerializer) { CameraPosition() }

    override val mapState = runtime.createMapState(
        baseStyle = BaseStyle.Json(BASE_STYLE),
        cameraPosition = savedCameraPosition,
    ) {
        MainMapContent(this)
    }

    init {
        // Save the MapLibre camera position for restoration after process recreation.
        viewModelScope.launch {
            snapshotFlow { mapState.cameraPosition }.collect { camera ->
                savedCameraPosition = camera
            }
        }
        // Apply the MapLibre viewport to StreetComplete's quest and overlay data sources.
        viewModelScope.launch {
            snapshotFlow { mapState.cameraPosition.zoom to mapState.viewport?.visibleBounds }
                .collect { (zoom, bounds) ->
                    val displayedArea = bounds?.toStreetCompleteBoundingBox()
                    mapQuestPinsSource.onMapMoved(zoom, displayedArea)
                    styleableOverlaySource.onMapMoved(zoom, displayedArea)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val downloadedTiles = callbackFlow {
        val listener = object : DownloadedTilesSource.Listener {
            override fun onUpdated() { trySend(Unit) }
        }
        downloadedTilesSource.addListener(listener)
        trySend(Unit)
        awaitClose {
            downloadedTilesSource.removeListener(listener)
        }
    }.buffer(Channel.CONFLATED).mapLatest {
        getDownloadedTiles()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override val questPins = mapQuestPinsSource.pins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun onClickQuest(properties: JsonObject): ClickResult {
        val key = mapQuestPinsSource.getQuestKey(properties) ?: return ClickResult.Pass
        emitClick(MainMapClick.Quest(key))
        return ClickResult.Consume
    }

    override val editHistoryPins = editHistoryPinsSource.pins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun onClickEdit(properties: JsonObject): ClickResult {
        val key = editHistoryPinsSource.getEditKey(properties) ?: return ClickResult.Pass
        emitClick(MainMapClick.Edit(key))
        return ClickResult.Consume
    }

    override val styleableElements = styleableOverlaySource.styledElements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun onClickElement(properties: JsonObject): ClickResult {
        val key = styleableOverlaySource.getElementKey(properties) ?: return ClickResult.Pass
        emitClick(MainMapClick.Element(key))
        return ClickResult.Consume
    }

    private fun emitClick(click: MainMapClick) {
        viewModelScope.launch { _clicks.emit(click) }
    }

    override fun onCleared() {
        mapState.close()
    }

    private suspend fun getDownloadedTiles() = withContext(Dispatchers.IO) {
        downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
    }
}
