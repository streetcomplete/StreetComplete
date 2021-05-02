package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** ~key(word)? */
class HasKeyLike(val key: String) : ElementFilter {
    private val regex = RegexOrSet.from(key)

    override fun toOverpassQLString() = "[" + "~" + "^($key)$".quoteIfNecessary() + " ~ '.*']"
    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { regex.matches(it) } != null
}
