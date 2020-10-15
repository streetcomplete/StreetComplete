package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary

/** key = value */
class HasTag(val key: String, val value: String) : ElementFilter {
    override fun toOverpassQLString() = "[" + key.quoteIfNecessary() + " = " + value.quoteIfNecessary() + "]"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key) == value
}