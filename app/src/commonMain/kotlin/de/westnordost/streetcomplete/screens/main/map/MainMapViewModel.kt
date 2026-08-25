package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.toGeoJsonFeatures
import de.westnordost.streetcomplete.screens.main.map.sources.EditHistoryPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.MapQuestPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.StyleableOverlaySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.sources.ComputedSource
import org.maplibre.compose.sources.ComputedSourceOptions
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

abstract class MainMapViewModel : ViewModel() {
    /** Downloaded areas */
    abstract val downloadedTiles: StateFlow<Collection<TilePos>>

    /** Quest pins in current view */
    abstract val questPins: StateFlow<Collection<Pin>>
    abstract fun getQuestKey(properties: JsonObject): QuestKey?

    /** Edit history pins in current view */
    abstract val editHistoryPins: StateFlow<Collection<Pin>>
    abstract fun getEditKey(properties: JsonObject): EditKey?

    /** Styled elements (of overlay) in current view */
    abstract val styleableElements: StateFlow<Collection<StyledElement>>
    abstract fun getElementKey(properties: JsonObject): ElementKey?

    abstract fun onMapMoved(cameraState: CameraState)
}

class MainMapViewModelImpl(
    private val downloadedTilesSource: DownloadedTilesSource,
    private val mapQuestPinsSource: MapQuestPinsSource,
    private val editHistoryPinsSource: EditHistoryPinsSource,
    private val styleableOverlaySource: StyleableOverlaySource,
) : MainMapViewModel() {

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

    override fun getQuestKey(properties: JsonObject): QuestKey? =
        mapQuestPinsSource.getQuestKey(properties)

    override val editHistoryPins: StateFlow<Collection<Pin>>
        get() = editHistoryPinsSource
            .pins
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    override fun getEditKey(properties: JsonObject): EditKey? =
        editHistoryPinsSource.getEditKey(properties)

    override val styleableElements: StateFlow<Collection<StyledElement>>
        get() = styleableOverlaySource.styledElements

    override fun getElementKey(properties: JsonObject): ElementKey? =
        styleableOverlaySource.getElementKey(properties)

    override fun onCleared() {
        styleableOverlaySource.onDestroy()
        mapQuestPinsSource.onDestroy()
    }

    override fun onMapMoved(cameraState: CameraState) {
        mapQuestPinsSource.onMapMoved(cameraState)
        styleableOverlaySource.onMapMoved(cameraState)
    }

    private suspend fun getDownloadedTiles() = withContext(Dispatchers.IO) {
        downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
    }
}
