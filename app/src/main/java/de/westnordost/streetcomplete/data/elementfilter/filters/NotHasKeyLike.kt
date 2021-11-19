package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** !~key(word)? */
class NotHasKeyLike(val key: String) : ElementFilter {
    private val regex = RegexOrSet.from(key)

    override fun toString() = "!~$key"
    override fun matches(obj: Element) = obj.tags.keys.none { regex.matches(it) }
}
