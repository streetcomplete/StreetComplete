package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

private val filterExpCache: MutableMap<String, ElementFilterExpression> = HashMap()

fun MapData.filter(expr: String): Sequence<Element> {
    expr.intern()
    if (!filterExpCache.containsKey(expr)) {
        synchronized(filterExpCache) {
            if (!filterExpCache.containsKey(expr)) {
                filterExpCache[expr] = expr.toElementFilterExpression()
            }
        }
    }
    return filter(filterExpCache.getValue(expr))
}

fun MapData.filter(expr: ElementFilterExpression): Sequence<Element> {
    return sequence {
        if (expr.includesElementType(ElementType.NODE)) yieldAll(nodes)
        if (expr.includesElementType(ElementType.WAY)) yieldAll(ways)
        if (expr.includesElementType(ElementType.RELATION)) yieldAll(relations)
    }.filter(expr::matches)
}
