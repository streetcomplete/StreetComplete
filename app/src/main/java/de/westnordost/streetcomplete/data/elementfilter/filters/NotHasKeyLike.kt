package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** !~key(word)? */
class NotHasKeyLike(val key: String) : ElementFilter {
    private val regex = RegexOrSet.from(key)

    override fun toOverpassQLString(): String {
        // not supported (conveniently) by overpass (yet): https://github.com/drolbr/Overpass-API/issues/589
        //return "[" + "!~" + "^(${key.pattern})$".quoteIfNecessary() + " ~ '.*']"
        throw UnsupportedOperationException()
    }

    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.keys?.find { regex.matches(it) } == null
}
