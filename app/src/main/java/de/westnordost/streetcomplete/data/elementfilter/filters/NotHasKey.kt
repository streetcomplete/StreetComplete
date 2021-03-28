package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary

/** !key */
class NotHasKey(val key: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + "!" + key.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = !(obj?.tags?.containsKey(key) ?: true)
}