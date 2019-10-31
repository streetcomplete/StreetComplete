package de.westnordost.streetcomplete.data.osm.tql

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.RELATIONS

/** Represents a parse result of a string in filter syntax, i.e.
 *  "ways with (highway = residential or highway = tertiary) and !name"  */
class TagFilterExpression(
    private val elementsTypes: List<ElementsTypeFilter>,
    private val tagExprRoot: BooleanExpression<TagFilter, Tags>?
) {
    private val overpassQuery: String

    init {
        val isNwr = elementsTypes.size == 3 && elementsTypes.containsAll(ElementsTypeFilter.values().asList())
        val oqlElementNames = if (isNwr) listOf("nwr") else elementsTypes.map { it.oqlName }
        overpassQuery = OverpassQueryCreator(oqlElementNames, tagExprRoot).create()
    }

    /** returns whether the given element is found through (=matches) this expression */
    fun matches(element: Element) =
        when (element.type) {
            Element.Type.NODE -> elementsTypes.contains(NODES)
            Element.Type.WAY -> elementsTypes.contains(WAYS)
            Element.Type.RELATION -> elementsTypes.contains(RELATIONS)
            else -> false
        } && (tagExprRoot?.matches(element.tags) ?: true)

    /** returns this expression as a Overpass query string */
    fun toOverpassQLString(): String {
        return overpassQuery
    }

}
