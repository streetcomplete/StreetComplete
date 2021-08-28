package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** !key */
class NotHasKey(val key: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + "!" + key.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = !(obj?.tags?.containsKey(key) ?: true)
}
