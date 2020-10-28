package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.util.MultiIterable

/** Quest type that's based on a simple element filter expression */
abstract class OsmFilterQuestType<T> : OsmElementQuestType<T> {

    val filter by lazy { elementFilter.toElementFilterExpression() }

    protected abstract val elementFilter: String

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        /* this is a considerate performance improvement over just iterating over the whole MapData
        *  because for quests that only filter for one (or two) element types, any filter checks
        *  are completely avoided */
        val iterable = MultiIterable<Element>()
        if (filter.includesElementType(Element.Type.NODE)) iterable.add(mapData.nodes)
        if (filter.includesElementType(Element.Type.WAY)) iterable.add(mapData.ways)
        if (filter.includesElementType(Element.Type.RELATION)) iterable.add(mapData.relations)
        return iterable.filter { element -> filter.matches(element) }
    }

    override fun isApplicableTo(element: Element) = filter.matches(element)
}
