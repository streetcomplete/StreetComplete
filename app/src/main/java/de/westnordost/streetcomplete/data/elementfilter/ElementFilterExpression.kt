package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.RELATIONS
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.util.EnumSet

/** Represents a parse result of a string in filter syntax, i.e.
 *  "ways with (highway = residential or highway = tertiary) and !name"  */
class ElementFilterExpression(
    private val elementsTypes: EnumSet<ElementsTypeFilter>,
    private val elementExprRoot: BooleanExpression<ElementFilter, Element>?
) {
    /** returns whether the given element is found through (=matches) this expression */
    fun matches(element: Element): Boolean =
        includesElementType(element.type) && (elementExprRoot?.matches(element) ?: true)

    fun includesElementType(elementType: ElementType): Boolean = when (elementType) {
        ElementType.NODE -> elementsTypes.contains(NODES)
        ElementType.WAY -> elementsTypes.contains(WAYS)
        ElementType.RELATION -> elementsTypes.contains(RELATIONS)
    }

    /** returns this expression as a Overpass query string */
    fun toOverpassQLString(): String = OverpassQueryCreator(elementsTypes, elementExprRoot).create()
}

/** Enum that specifies which type(s) of elements to retrieve  */
enum class ElementsTypeFilter { NODES, WAYS, RELATIONS }
