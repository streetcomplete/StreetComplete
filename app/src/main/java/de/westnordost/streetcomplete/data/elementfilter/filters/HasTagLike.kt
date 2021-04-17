package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** ~key(word)? ~ val(ue)? */
class HasTagLike(key: String, value: String) : ElementFilter {
    val key = key.toRegex()
    val value = value.toRegex()

    override fun toOverpassQLString() =
        "[" + "~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ " + "^(${value.pattern})$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?) =
        obj?.tags?.entries?.find { it.key.matches(key) && it.value.matches(value) } != null
}
