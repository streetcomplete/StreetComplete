package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.RELATIONS
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter

/** Represents a parse result of a string in filter syntax, i.e.
 *  "ways with (highway = residential or highway = tertiary) and !name"  */
class ElementFilterExpression(
    private val elementsTypes: List<ElementsTypeFilter>,
    private val elementExprRoot: BooleanExpression<ElementFilter, Element>?
) {
    /** returns whether the given element is found through (=matches) this expression */
    fun matches(element: Element) =
        when (element.type) {
            Element.Type.NODE -> elementsTypes.contains(NODES)
            Element.Type.WAY -> elementsTypes.contains(WAYS)
            Element.Type.RELATION -> elementsTypes.contains(RELATIONS)
            else -> false
        } && (elementExprRoot?.matches(element) ?: true)

    /** returns this expression as a Overpass query string */
    fun toOverpassQLString(): String = OverpassQueryCreator(elementsTypes, elementExprRoot).create()
}

/** Enum that specifies which type(s) of elements to retrieve  */
enum class ElementsTypeFilter { NODES, WAYS, RELATIONS }