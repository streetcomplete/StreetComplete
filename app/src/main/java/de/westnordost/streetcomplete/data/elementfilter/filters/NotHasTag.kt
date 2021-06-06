package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** key != value */
class NotHasTag(val key: String, val value: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + key.quoteIfNecessary() + " != " + value.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key) != value
}
