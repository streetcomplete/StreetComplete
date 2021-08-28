package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary
import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** ~key(word)? ~ val(ue)? */
class HasTagLike(val key: String, val value: String) : ElementFilter {
    private val keyRegex = RegexOrSet.from(key)
    private val valueRegex = RegexOrSet.from(value)

    override fun toOverpassQLString() =
        "[" + "~" + "^($key)$".quoteIfNecessary() + " ~ " + "^($value)$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?) =
        obj?.tags?.entries?.find { keyRegex.matches(it.key) && valueRegex.matches(it.value) } != null
}
