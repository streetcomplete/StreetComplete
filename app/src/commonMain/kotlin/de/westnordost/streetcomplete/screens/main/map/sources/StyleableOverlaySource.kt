package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.isDisabled
import de.westnordost.streetcomplete.screens.main.map.layers.toElementKey
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

@OptIn(ExperimentalCoroutinesApi::class)
class StyleableOverlaySource(
    private val selectedOverlaySource: SelectedOverlaySource,
    private val mapDataWithEditsSource: MapDataWithEditsSource,
) {
    private val displayedRect = MutableStateFlow<TilesRect?>(null)

    val styledElements: Flow<Collection<StyledElement>> = displayedRect.flatMapLatest { rect ->
        if (rect == null) return@flatMapLatest flowOf(emptyList())
        val view = ElementsInView(rect.asBoundingBox(TILES_ZOOM))
        events().map { event ->
            when (event) {
                Event.Reload -> view.reload()
                Event.Clear -> view.clear()
                is Event.Updated -> view.update(event.updated, event.deleted)
            }
            view.elements
        }
    }.flowOn(Dispatchers.Default)

    /** The styled elements of the selected overlay in one displayed area */
    private inner class ElementsInView(private val bbox: BoundingBox) {
        private val elementsByKey = mutableMapOf<ElementKey, StyledElement>()
        private var overlay: Overlay? = null

        val elements: Collection<StyledElement> get() = elementsByKey.values.toList()

        suspend fun reload() {
            val (selected, data) = withContext(Dispatchers.IO) {
                val selected = selectedOverlaySource.selectedOverlay
                selected to selected?.let {
                    createStyledElementsByKey(it, mapDataWithEditsSource.getMapDataWithGeometry(bbox)).toMap()
                }
            }
            overlay = selected
            elementsByKey.clear()
            if (data != null) elementsByKey.putAll(data)
        }

        fun clear() {
            elementsByKey.clear()
        }

        fun update(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            deleted.forEach { elementsByKey.remove(it) }
            updated.forEach { elementsByKey.remove(it.key) }
            val selected = overlay ?: return
            createStyledElementsByKey(selected, updated).forEach { (key, element) ->
                if (bbox.intersect(element.geometry.bounds)) elementsByKey[key] = element
            }
        }
    }

    private fun events(): Flow<Event> = callbackFlow {
        val overlayListener = object : SelectedOverlaySource.Listener {
            override fun onSelectedOverlayChanged() { trySend(Event.Reload) }
        }
        val dataListener = object : MapDataWithEditsSource.Listener {
            override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
                trySend(Event.Updated(updated, deleted.toList()))
            }
            override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
                trySend(Event.Reload)
            }
            override fun onCleared() { trySend(Event.Clear) }
        }
        selectedOverlaySource.addListener(overlayListener)
        mapDataWithEditsSource.addListener(dataListener)
        trySend(Event.Reload)
        awaitClose {
            selectedOverlaySource.removeListener(overlayListener)
            mapDataWithEditsSource.removeListener(dataListener)
        }
    }.buffer(Channel.UNLIMITED)

    fun onMapMoved(zoom: Double, displayedArea: BoundingBox?) {
        if (displayedArea == null) {
            displayedRect.value = null
            return
        }
        // Keep the loaded data when zooming out.
        if (zoom < MIN_ZOOM) return
        val rect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (rect.size > 32) return
        if (displayedRect.value?.contains(rect) != true) displayedRect.value = rect
    }

    private fun createStyledElementsByKey(
        overlay: Overlay,
        mapData: MapDataWithGeometry
    ): Sequence<Pair<ElementKey, StyledElement>> =
        overlay.getStyledElements(mapData).mapNotNull { (element, style) ->
            val key = element.key
            val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
            key to StyledElement(element, geometry, style)
        }

    private sealed interface Event {
        data object Reload : Event
        data object Clear : Event
        data class Updated(val updated: MapDataWithGeometry, val deleted: List<ElementKey>) : Event
    }

    fun getElementKey(properties: JsonObject): ElementKey? =
        if (!properties.isDisabled()) properties.toElementKey() else null

    companion object {
        private const val TILES_ZOOM = 16
        private const val MIN_ZOOM = 14
    }
}
