package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.sources.EditHistoryPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.MapQuestPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.StyleableOverlaySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

abstract class MainMapViewModel : ViewModel() {
    abstract val selectedOverlay: StateFlow<Overlay?>

    abstract fun onViewportChanged(zoom: Double, bounds: BoundingBox?)

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
}

class MainMapViewModelImpl(
    private val downloadedTilesSource: DownloadedTilesSource,
    private val mapQuestPinsSource: MapQuestPinsSource,
    private val editHistoryPinsSource: EditHistoryPinsSource,
    private val styleableOverlaySource: StyleableOverlaySource,
    private val selectedOverlaySource: SelectedOverlaySource,
) : MainMapViewModel() {

    override val selectedOverlay = callbackFlow {
        val listener = object : SelectedOverlaySource.Listener {
            override fun onSelectedOverlayChanged() { trySend(selectedOverlaySource.selectedOverlay) }
        }
        selectedOverlaySource.addListener(listener)
        trySend(selectedOverlaySource.selectedOverlay)
        awaitClose { selectedOverlaySource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), null)

    override fun onViewportChanged(zoom: Double, bounds: BoundingBox?) {
        mapQuestPinsSource.onMapMoved(zoom, bounds)
        styleableOverlaySource.onMapMoved(zoom, bounds)
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
        withContext(Dispatchers.IO) {
            downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override val questPins = mapQuestPinsSource.pins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun getQuestKey(properties: JsonObject) = mapQuestPinsSource.getQuestKey(properties)

    override val editHistoryPins = editHistoryPinsSource.pins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun getEditKey(properties: JsonObject) = editHistoryPinsSource.getEditKey(properties)

    override val styleableElements = styleableOverlaySource.styledElements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    override fun getElementKey(properties: JsonObject) = styleableOverlaySource.getElementKey(properties)
}
