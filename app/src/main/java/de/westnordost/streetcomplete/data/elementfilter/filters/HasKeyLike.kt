package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** ~key(word)? */
class HasKeyLike(key: String) : ElementFilter {
    val key = key.toRegex()

    override fun toOverpassQLString() = "[" + "~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ '.*']"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { it.matches(key) } != null
}
