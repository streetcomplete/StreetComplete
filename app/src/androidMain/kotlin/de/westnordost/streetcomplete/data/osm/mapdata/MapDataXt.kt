package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

private val filterExpCache: MutableMap<String, ElementFilterExpression> = HashMap()

fun MapData.filter(expr: String): Sequence<Element> =
    filter(getElementFilterExpression(expr))

fun MapData.filter(expr: ElementFilterExpression): Sequence<Element> {
    /* this is a considerate performance improvement over just iterating over the whole MapData
     * because filters that only include one (or two) element types, any filter checks
     * are completely avoided */
    return sequence {
        if (expr.includesElementType(ElementType.NODE)) yieldAll(nodes)
        if (expr.includesElementType(ElementType.WAY)) yieldAll(ways)
        if (expr.includesElementType(ElementType.RELATION)) yieldAll(relations)
    }.filter(expr::matches)
}

private fun getElementFilterExpression(expr: String): ElementFilterExpression = synchronized(filterExpCache) {
    if (!filterExpCache.containsKey(expr)) {
        filterExpCache[expr] = expr.toElementFilterExpression()
    }
    return filterExpCache.getValue(expr)
}
