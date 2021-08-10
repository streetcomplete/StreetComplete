package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

/** Quest type that's based on a simple element filter expression */
abstract class OsmFilterQuestType<T> : OsmElementQuestType<T> {

    val filter by lazy { elementFilter.toElementFilterExpression() }

    protected abstract val elementFilter: String

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        /* this is a considerate performance improvement over just iterating over the whole MapData
        *  because for quests that only filter for one (or two) element types, any filter checks
        *  are completely avoided */
        return sequence {
            if (filter.includesElementType(ElementType.NODE)) yieldAll(mapData.nodes)
            if (filter.includesElementType(ElementType.WAY)) yieldAll(mapData.ways)
            if (filter.includesElementType(ElementType.RELATION)) yieldAll(mapData.relations)
        }.filter { filter.matches(it) }.asIterable()
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
