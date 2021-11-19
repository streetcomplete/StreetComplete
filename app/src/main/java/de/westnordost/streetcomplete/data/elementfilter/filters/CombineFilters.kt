package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** Filter that combines several other filters */
class CombineFilters(vararg var filters: ElementFilter) : ElementFilter {
    override fun toString() = filters.joinToString(" and ")
    override fun matches(obj: Element) = filters.all { it.matches(obj) }
}
