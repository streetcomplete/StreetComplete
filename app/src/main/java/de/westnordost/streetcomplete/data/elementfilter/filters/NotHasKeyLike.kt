package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element

/** !~key(word)? */
class NotHasKeyLike(key: String) : ElementFilter {
    val key = key.toRegex()

    override fun toOverpassQLString(): String {
        // not supported (conveniently) by overpass (yet): https://github.com/drolbr/Overpass-API/issues/589
        //return "[" + "!~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ '.*']"
        throw UnsupportedOperationException()
    }

    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { it.matches(key) } == null
}