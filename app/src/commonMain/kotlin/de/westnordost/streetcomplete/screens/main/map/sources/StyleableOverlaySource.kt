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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

/** Source for styled map data (consisting of [StyledElement]s), see [styledElements]) on the map.
 *  Since there can be a very, very large number of these on the map, we only show those that are in
 *  view. This requires users to call [onMapMoved] so that the [styledElements] are updated when the
 *  viewport moves to a new area. */
@OptIn(ExperimentalCoroutinesApi::class)
class StyleableOverlaySource(
    private val selectedOverlaySource: SelectedOverlaySource,
    private val mapDataWithEditsSource: MapDataWithEditsSource,
) {
    private val displayedRect = MutableStateFlow<TilesRect?>(null)

    val styledElements: Flow<Collection<StyledElement>> = flow {
        val elementsByKey = mutableMapOf<ElementKey, StyledElement>()
        var overlay: Overlay? = null

        emitAll(displayedRect.flatMapLatest { rect ->
            if (rect == null) return@flatMapLatest flowOf(emptyList())
            val bbox = rect.asBoundingBox(TILES_ZOOM)
            events().map { event ->
                when (event) {
                    Event.Reload -> {
                        val selected = selectedOverlaySource.selectedOverlay
                        overlay = selected

                        elementsByKey.clear()
                        if (selected != null) {
                            val mapData = withContext(Dispatchers.IO) {
                                mapDataWithEditsSource.getMapDataWithGeometry(bbox)
                            }
                            val styledElements = mapData.toStyledElements(selected)
                            styledElements.forEach { elementsByKey[it.element.key] = it }
                        }
                    }
                    Event.Clear -> {
                        elementsByKey.clear()
                    }
                    is Event.Updated -> {
                        val selected = overlay
                        if (selected != null) {
                            event.deleted.forEach { elementsByKey.remove(it) }
                            event.updated.forEach { elementsByKey.remove(it.key) }

                            val styledElements = event.updated.toStyledElements(selected)
                            for (styledElement in styledElements) {
                                if (bbox.intersect(styledElement.geometry.bounds)) {
                                    elementsByKey[styledElement.element.key] = styledElement
                                }
                            }
                        }
                    }
                }
                elementsByKey.values.toList()
            }
        })
    }.flowOn(Dispatchers.Default)

    private sealed interface Event {
        data object Reload : Event
        data object Clear : Event
        data class Updated(val updated: MapDataWithGeometry, val deleted: List<ElementKey>) : Event
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

    fun getElementKey(properties: JsonObject): ElementKey? =
        if (!properties.isDisabled()) properties.toElementKey() else null

    companion object {
        private const val TILES_ZOOM = 16
        private const val MIN_ZOOM = 14
    }
}

private fun MapDataWithGeometry.toStyledElements(overlay: Overlay): Sequence<StyledElement> =
    overlay.getStyledElements(this).mapNotNull { (element, style) ->
        val geometry = getGeometry(element.type, element.id) ?: return@mapNotNull null
        StyledElement(element, geometry, style)
    }
