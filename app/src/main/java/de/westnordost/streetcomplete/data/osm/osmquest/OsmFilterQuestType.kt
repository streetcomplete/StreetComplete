package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

/** Quest type that's based on a simple element filter expression */
abstract class OsmFilterQuestType<T> : OsmElementQuestType<T> {

    val filter by lazy { elementFilter.toElementFilterExpression() }

    protected abstract val elementFilter: String

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        /* this is a considerate performance improvement over just iterating over the whole MapData
        *  because for quests that only filter for one (or two) element types, any filter checks
        *  are completely avoided */
        return sequence {
            if (filter.includesElementType(Element.Type.NODE)) yieldAll(mapData.nodes)
            if (filter.includesElementType(Element.Type.WAY)) yieldAll(mapData.ways)
            if (filter.includesElementType(Element.Type.RELATION)) yieldAll(mapData.relations)
        }.filter { filter.matches(it) }.asIterable()
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
