package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.NODES
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.RELATIONS
import de.westnordost.streetcomplete.data.elementfilter.ElementsTypeFilter.WAYS
import de.westnordost.streetcomplete.data.elementfilter.filters.CombineFilters
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareDateTagValue
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareElementAge
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareTagAge
import de.westnordost.streetcomplete.data.elementfilter.filters.CompareTagValue
import de.westnordost.streetcomplete.data.elementfilter.filters.ElementFilter
import de.westnordost.streetcomplete.data.elementfilter.filters.HasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.HasKeyLike
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTag
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagLike
import de.westnordost.streetcomplete.data.elementfilter.filters.HasTagValueLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKey
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasKeyLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTag
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTagValueLike
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.util.EnumSet

/** Represents a parse result of a string in filter syntax, i.e.
 *  "ways with (highway = residential or highway = tertiary) and !name"  */
class ElementFilterExpression(
    private val elementsTypes: EnumSet<ElementsTypeFilter>,
    private val elementExprRoot: BooleanExpression<ElementFilter, Element>?
) {
    /* Performance improvement: Allows to skip early on elements that have no tags at all */
    private val mayEvaluateToTrueWithNoTags = elementExprRoot?.mayEvaluateToTrueWithNoTags ?: true

    /** returns whether the given element is found through (=matches) this expression */
    fun matches(element: Element): Boolean =
        includesElementType(element.type)
        && (element.tags.isNotEmpty() || mayEvaluateToTrueWithNoTags)
        && (elementExprRoot?.matches(element) ?: true)

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

private val BooleanExpression<ElementFilter, Element>.mayEvaluateToTrueWithNoTags: Boolean
get() = when(this) {
    is Leaf -> value.mayEvaluateToTrueWithNoTags
    is AnyOf -> children.any { it.mayEvaluateToTrueWithNoTags }
    is AllOf -> children.all { it.mayEvaluateToTrueWithNoTags }
    else -> throw IllegalStateException("Unexpected expression")
}

private val ElementFilter.mayEvaluateToTrueWithNoTags: Boolean get() = when(this) {
    is CompareElementAge,
    is CompareTagAge ->
        true
    is NotHasKey,
    is NotHasKeyLike,
    is NotHasTag,
    is NotHasTagValueLike,
    is HasTagValueLike ->
        true
    is HasKey,
    is HasKeyLike,
    is HasTag,
    is HasTagLike,
    is CompareTagValue,
    is CompareDateTagValue ->
        false
    is CombineFilters ->
        filters.all { it.mayEvaluateToTrueWithNoTags }
}
