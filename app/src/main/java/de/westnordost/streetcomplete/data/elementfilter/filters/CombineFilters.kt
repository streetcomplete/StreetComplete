package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element

class CombineFilters(vararg var filters: ElementFilter) : ElementFilter {
    override fun toOverpassQLString() = filters.joinToString("") { it.toOverpassQLString() }
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = filters.all { it.matches(obj) }
}
