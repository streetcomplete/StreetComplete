package de.westnordost.streetcomplete.data.osm.tql

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.osm.tql.ElementsTypeFilter.RELATIONS

/** Represents a parse result of a string in filter syntax, i.e.
 *  "ways with (highway = residential or highway = tertiary) and !name"  */
class TagFilterExpression(
    private val elementsTypeFilters: List<ElementsTypeFilter>,
    private val tagExprRoot: BooleanExpression<OQLExpressionValue>
) {

    /** returns whether the given element is found through (=matches) this expression */
    fun matches(element: Element) =
        when (element.type) {
	        Element.Type.NODE -> elementsTypeFilters.contains(NODES)
	        Element.Type.WAY -> elementsTypeFilters.contains(WAYS)
	        Element.Type.RELATION -> elementsTypeFilters.contains(RELATIONS)
	        else -> false
        } && tagExprRoot.matches(element)

    /** returns this expression as a Overpass query string */
    fun toOverpassQLString(bbox: BoundingBox?): String {
        val oql = StringBuilder()
        if (bbox != null) {
            oql.append(bbox.toGlobalOverpassBBox())
        }

        val expandedExpression = createExpandedExpression()

        val elements = expandedExpression.firstChild.toOverpassQLFilters()

        val isNwr =
            elementsTypeFilters.size == 3 && elementsTypeFilters.containsAll(listOf(*ElementsTypeFilter.values()))

        val useUnion = !isNwr && elementsTypeFilters.size > 1 || elements.size > 1

        if (useUnion) oql.append("(")
        if (isNwr) {
            oql.append(getTagFiltersOverpassQLString("nwr", elements))
        } else {
            for (filter in elementsTypeFilters) {
                oql.append(getTagFiltersOverpassQLString(filter.oqlName, elements))
            }
        }
        if (useUnion) oql.append(");")

        return oql.toString()
    }

    private fun createExpandedExpression(): BooleanExpression<OQLExpressionValue> {
        val result = tagExprRoot.copy()
        result.flatten()
        result.expand()
        return result
    }

    private fun getTagFiltersOverpassQLString(elementTypeName: String, elements: List<String>) =
	    elements.joinToString("") { element -> "$elementTypeName$element;" }

    private fun BooleanExpression<OQLExpressionValue>?.toOverpassQLFilters() = when {
	    this == null     -> listOf("")
	    isOr             -> children.map { it.toSingleOverpassQLFilter() }
	    isAnd || isValue -> listOf(toSingleOverpassQLFilter())
	    else             -> throw RuntimeException("The boolean expression is not in the expected format")
    }

    private fun BooleanExpression<OQLExpressionValue>.toSingleOverpassQLFilter() = when {
	    isValue -> "[${value!!.toOverpassQLString()}]"
	    isAnd   -> children.joinToString("") { "[${it.value!!.toOverpassQLString()}]" }
	    else    -> throw RuntimeException("The boolean expression is not in the expected format")
    }
}
