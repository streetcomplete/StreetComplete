package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_address_title
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.sources.StyleableOverlaySource
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.DrawableResource

/** Runs the production overlay source/cancellation path against deterministic in-memory map data. */
internal class MapPerformanceOverlayPipeline(
    icons: List<DrawableResource>,
    elementCount: Int,
) {
    private val selectedOverlaySource = MapPerformanceSelectedOverlaySource()
    private val source = StyleableOverlaySource(
        selectedOverlaySource = selectedOverlaySource,
        mapDataSource = MapPerformanceMapDataSource(elementCount),
        workerDispatcher = Dispatchers.Default,
    )
    private val overlay = MapPerformanceOverlay(icons)

    val styledElements: StateFlow<List<StyledElement>> = source.styledElements

    fun setPresented(presented: Boolean) = source.setActive(presented)

    fun select() = selectedOverlaySource.set(overlay)

    fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) =
        source.onViewportChanged(zoom, displayedArea)

    fun close() = source.close()
}

private class MapPerformanceSelectedOverlaySource : SelectedOverlaySource {
    private val value = atomic<Overlay?>(null)
    private val listenerLock = ReentrantLock()
    private val listeners = mutableSetOf<SelectedOverlaySource.Listener>()

    override val selectedOverlay: Overlay? get() = value.value

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listenerLock.withLock { listeners += listener }
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listenerLock.withLock { listeners -= listener }
    }

    fun set(overlay: Overlay?) {
        if (value.getAndSet(overlay) === overlay) return
        listenerLock.withLock { listeners.toList() }.forEach {
            it.onSelectedOverlayChanged()
        }
    }
}

private class MapPerformanceMapDataSource(
    private val elementCount: Int,
) : MapDataWithEditsSource {
    override fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry {
        val result = MutableMapDataWithGeometry()
        val rows = 10
        repeat(elementCount) { index ->
            val row = index / rows
            val column = index % rows
            val latitudeFraction = (row + 1.0) / (elementCount / rows + 1.0)
            val longitudeFraction = (column + 1.0) / (rows + 1.0)
            val position = de.westnordost.streetcomplete.data.osm.mapdata.LatLon(
                latitude = bbox.min.latitude +
                    (bbox.max.latitude - bbox.min.latitude) * latitudeFraction,
                longitude = bbox.min.longitude +
                    (bbox.max.longitude - bbox.min.longitude) * longitudeFraction,
            )
            val node = Node(index.toLong() + 1L, position)
            result.put(node, ElementPointGeometry(position))
        }
        return result
    }

    override fun addListener(listener: MapDataWithEditsSource.Listener) = Unit
    override fun removeListener(listener: MapDataWithEditsSource.Listener) = Unit
    override fun getGeometry(type: ElementType, id: Long): ElementGeometry? = null
    override fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> = emptyList()
    override fun getNode(id: Long): Node? = null
    override fun getWay(id: Long): Way? = null
    override fun getRelation(id: Long): Relation? = null
    override fun getWayComplete(id: Long): MapData? = null
    override fun getRelationComplete(id: Long): MapData? = null
    override fun getWaysForNode(id: Long): Collection<Way> = emptyList()
    override fun getRelationsForNode(id: Long): Collection<Relation> = emptyList()
    override fun getRelationsForWay(id: Long): Collection<Relation> = emptyList()
    override fun getRelationsForRelation(id: Long): Collection<Relation> = emptyList()
}

private class MapPerformanceOverlay(
    private val icons: List<DrawableResource>,
) : Overlay {
    override val changesetComment = "map performance scenario"
    override val icon = icons.first()
    override val title = Res.string.quest_address_title
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = emptyList()

    override fun getStyledElements(
        mapData: MapDataWithGeometry,
    ): Sequence<Pair<Element, OverlayStyle>> = mapData.asSequence().map { element ->
        element to OverlayStyle.Point(
            icon = icons[(element.id % icons.size).toInt()],
            label = "Overlay ${element.id}",
        )
    }

    @Composable
    override fun Form(
        on: (OverlayAction) -> Unit,
        element: Element?,
        geometry: ElementGeometry,
        countryInfo: CountryInfo,
    ) = Unit
}
