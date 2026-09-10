package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
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
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraPosition
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

    abstract val clicks: SharedFlow<MainMapClick>
    abstract fun zoomToCluster(zoom: Double)

    /** Downloaded areas */
    abstract val downloadedTiles: StateFlow<Collection<TilePos>>

    /** Quest pins in current view */
    abstract val questPins: StateFlow<Collection<Pin>>
    abstract fun onClickQuest(properties: JsonObject)

    /** Edit history pins in current view */
    abstract val editHistoryPins: StateFlow<Collection<Pin>>
    abstract fun onClickEdit(properties: JsonObject)

    /** Styled elements (of overlay) in current view */
    abstract val styleableElements: StateFlow<Collection<StyledElement>>
    abstract fun onClickElement(properties: JsonObject)
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
) : MainMapViewModel() {

    override val location = MutableStateFlow<Location?>(null)
    override val rotation = MutableStateFlow<Float?>(null)
    override val isRecording = MutableStateFlow(false)
    override val trackpoints = MutableStateFlow<List<LatLon>>(emptyList())
    override val oldTrackpointsLists = MutableStateFlow<List<List<LatLon>>>(emptyList())
    override val shownBottomSheet = MutableStateFlow<ShownBottomSheet?>(null)
    override val shownMarkers = MutableStateFlow<Collection<Marker>?>(null)
    override val isShowingUndoHistorySidebar = MutableStateFlow(false)

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

    override val downloadedTiles = callbackFlow {
        val listener = object : DownloadedTilesSource.Listener {
            override fun onUpdated() { launch { send(getDownloadedTiles()) } }
        }
        send(getDownloadedTiles())
        downloadedTilesSource.addListener(listener)
        awaitClose {
            downloadedTilesSource.removeListener(listener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    override val questPins: StateFlow<Collection<Pin>>
        get() = mapQuestPinsSource.pins

    override fun onClickQuest(properties: JsonObject) {
        mapQuestPinsSource.getQuestKey(properties)?.let { emitClick(MainMapClick.Quest(it)) }
    }

    override val editHistoryPins: StateFlow<Collection<Pin>>
        get() = editHistoryPinsSource
            .pins
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    override fun onClickEdit(properties: JsonObject) {
        editHistoryPinsSource.getEditKey(properties)?.let { emitClick(MainMapClick.Edit(it)) }
    }

    override val styleableElements: StateFlow<Collection<StyledElement>>
        get() = styleableOverlaySource.styledElements

    override fun onClickElement(properties: JsonObject) {
        styleableOverlaySource.getElementKey(properties)?.let { emitClick(MainMapClick.Element(it)) }
    }

    private fun emitClick(click: MainMapClick) {
        viewModelScope.launch { _clicks.emit(click) }
    }

    override fun zoomToCluster(zoom: Double) {
        viewModelScope.launch {
            mapState.animateCameraPosition(mapState.cameraPosition.copy(zoom = zoom))
        }
    }

    override fun onCleared() {
        styleableOverlaySource.onDestroy()
        mapQuestPinsSource.onDestroy()
        mapState.close()
    }

    private suspend fun getDownloadedTiles() = withContext(Dispatchers.IO) {
        downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
    }
}
