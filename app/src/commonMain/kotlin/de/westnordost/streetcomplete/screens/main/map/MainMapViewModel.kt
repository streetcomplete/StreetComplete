package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.sources.DownloadedTilesStateSource
import de.westnordost.streetcomplete.screens.main.map.sources.EditHistoryPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.MapQuestPinsSource
import de.westnordost.streetcomplete.screens.main.map.sources.StyleableOverlaySource
import kotlinx.coroutines.flow.StateFlow

/** Owns renderer-independent live data shown by the shared main map. */
abstract class MainMapViewModel : ViewModel() {
    abstract val downloadedTiles: StateFlow<List<TilePos>>
    abstract val questPins: StateFlow<PinSnapshot>
    abstract val editHistoryPins: StateFlow<PinSnapshot>
    abstract val styleableElements: StateFlow<List<StyledElement>>

    abstract fun setPresented(presented: Boolean)
    abstract fun setActivePinMode(mode: MainMapPinMode)
    abstract fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?)
    abstract fun getQuestKey(properties: Map<String, String>): QuestKey?
    abstract fun getEditKey(properties: Map<String, String>): EditKey?
}

class MainMapViewModelImpl(
    private val downloadedTilesSource: DownloadedTilesStateSource,
    private val questPinsSource: MapQuestPinsSource,
    private val editHistoryPinsSource: EditHistoryPinsSource,
    private val styleableOverlaySource: StyleableOverlaySource,
) : MainMapViewModel() {
    private var isPresented = false
    private var requestedPinMode = MainMapPinMode.NONE

    override val downloadedTiles = downloadedTilesSource.tiles
    override val questPins = questPinsSource.pins
    override val editHistoryPins = editHistoryPinsSource.pins
    override val styleableElements = styleableOverlaySource.styledElements

    override fun setPresented(presented: Boolean) {
        if (isPresented == presented) return
        isPresented = presented
        downloadedTilesSource.setActive(presented)
        styleableOverlaySource.setActive(presented)
        updateActivePinSources()
    }

    override fun setActivePinMode(mode: MainMapPinMode) {
        if (requestedPinMode == mode) return
        requestedPinMode = mode
        updateActivePinSources()
    }

    override fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) {
        questPinsSource.onViewportChanged(zoom, displayedArea)
        styleableOverlaySource.onViewportChanged(zoom, displayedArea)
    }

    override fun getQuestKey(properties: Map<String, String>): QuestKey? =
        questPinsSource.getQuestKey(properties)

    override fun getEditKey(properties: Map<String, String>): EditKey? =
        editHistoryPinsSource.getEditKey(properties)

    override fun onCleared() {
        downloadedTilesSource.close()
        questPinsSource.close()
        editHistoryPinsSource.close()
        styleableOverlaySource.close()
    }

    private fun updateActivePinSources() {
        questPinsSource.setActive(isPresented && requestedPinMode == MainMapPinMode.QUESTS)
        editHistoryPinsSource.setActive(isPresented && requestedPinMode == MainMapPinMode.EDITS)
    }
}
