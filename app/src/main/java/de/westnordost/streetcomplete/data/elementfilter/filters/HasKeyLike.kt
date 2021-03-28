package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary

/** ~key(word)? */
class HasKeyLike(key: String) : ElementFilter {
    val key = key.toRegex()

    override fun toOverpassQLString() = "[" + "~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ '.*']"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { it.matches(key) } != null
}