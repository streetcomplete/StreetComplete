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
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTagLike
import de.westnordost.streetcomplete.data.elementfilter.filters.NotHasTagValueLike
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

/** Represents the parse result of a string in filter syntax, e.g.
 *
 *  `ways with (highway = residential or highway = tertiary) and !name`
 *
 *  [matches] of an [ElementFilterExpression] parsed from the above string returns true for any
 *  residential or tertiary roads that have no name tagged.
 *
 *  ### Cheatsheet for element filter syntax:
 *  | expression                     | [matches] returns `true` if element…                                          |
 *  | :----------------------------- | :---------------------------------------------------------------------------- |
 *  | `shop`                         | has a tag with key `shop`                                                     |
 *  | `!shop`                        | doesn't have a tag with key `shop`                                            |
 *  | `shop = car`                   | has a tag with key `shop` whose value is `car`                                |
 *  | `shop != car`                  | doesn't have a tag with key `shop` whose value is `car`                       |
 *  | `~shop|craft`                  | has a tag whose key matches the regex `shop|craft`                            |
 *  | `!~shop|craft`                 | doesn't have a tag whose key matches the regex `shop|craft`                   |
 *  | `shop ~ car|boat`              | has a tag whose key is `shop` and whose value matches the regex `car|boat`    |
 *  | `shop !~ car|boat`             | doesn't have a tag whose key is `shop` and value matches the regex `car|boat` |
 *  | `~shop|craft ~ car|boat`       | has a tag whose key matches `shop|craft` and value matches `car|boat` (both regexes) |
 *  | `~shop|craft !~ car|boat`      | doesn't have a tag whose key matches `shop|craft` and value matches `car|boat` (both regexes) |
 *  | `foo < 3.3`                    | has a tag with key `foo` whose value is smaller than 2.5<br/>`<`,`<=`,`>=`,`>` work likewise |
 *  | `foo < 3.3ft`                  | same as above but value is smaller than 3.3 feet (~1 meter)<br/>This works for other units as well (mph, st, lbs, yds...) |
 *  | `foo < 3'4"`                   | same as above but value is smaller than 3 feet, 4 inches (~1 meter)           |
 *  | `foo < 2012-10-01`             | same as above but value is a date older than Oct 1st 2012                     |
 *  | `foo < today -1.5 years`       | same as above but value is a date older than 1.5 years<br/>In place of `years`, `months`, `weeks` or `days` work |
 *  | `shop newer today -99 days`    | has a tag with key `shop` which has been modified in the last 99 days.<br/>Absolute dates work too. |
 *  | `shop older today -1 months`   | has a tag with key `shop` which hasn't been changed for more than a month.<br/>Absolute dates work too. |
 *  | `shop and name`                | has both a tag with key `shop` and one with key `name`                        |
 *  | `shop or craft`                | has either a tag with key `shop` or one with key `craft`                      |
 *  | `shop and (ref or name)`       | has a tag with key `shop` and either a tag with key `ref` or `name`           |
 *
 *  Note that regexes have to match the whole string, i.e. `~shop|craft` does not match `shop_type`.
 *
 *  ### Equivalent expressions
 *  | expression                     | equivalent expression                                    |
 *  | :----------------------------- | :------------------------------------------------------- |
 *  | `shop and shop = boat`         | `shop = boat`                                            |
 *  | `!shop or shop != boat`        | `shop != boat`                                           |
 *  | `shop = car or shop = boat`    | `shop ~ car|boat`                                        |
 *  | `craft or shop and name`       | `craft or (shop and name)` (`and` has higher precedence) |
 *  | `!(amenity and craft)`         | **<error>** (negation of expression not supported)       |
 *  */
class ElementFilterExpression(
    internal val elementsTypes: Set<ElementsTypeFilter>,
    internal val elementExprRoot: BooleanExpression<ElementFilter, Element>?
) {
    /* Performance improvement: Allows to skip early on elements that have no tags at all */
    val mayEvaluateToTrueWithNoTags = elementExprRoot?.mayEvaluateToTrueWithNoTags ?: true

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
}

/** Enum that specifies which type(s) of elements to retrieve  */
enum class ElementsTypeFilter { NODES, WAYS, RELATIONS }

private val BooleanExpression<ElementFilter, Element>.mayEvaluateToTrueWithNoTags: Boolean
    get() = when (this) {
        is Leaf -> value.mayEvaluateToTrueWithNoTags
        is AnyOf -> children.any { it.mayEvaluateToTrueWithNoTags }
        is AllOf -> children.all { it.mayEvaluateToTrueWithNoTags }
        else -> throw IllegalStateException("Unexpected expression")
    }

private val ElementFilter.mayEvaluateToTrueWithNoTags: Boolean get() = when (this) {
    is CompareElementAge,
    is CompareTagAge ->
        true
    is NotHasKey,
    is NotHasKeyLike,
    is NotHasTag,
    is NotHasTagValueLike,
    is HasTagValueLike,
    is NotHasTagLike ->
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
