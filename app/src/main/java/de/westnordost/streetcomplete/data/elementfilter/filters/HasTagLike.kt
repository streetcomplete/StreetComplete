package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** ~key(word)? ~ val(ue)? */
class HasTagLike(val key: String, val value: String) : ElementFilter {
    private val keyRegex = RegexOrSet.from(key)
    private val valueRegex = RegexOrSet.from(value)

    override fun toString() = "~$key ~ $value"

    override fun matches(obj: Element) =
        obj.tags.entries.any { keyRegex.matches(it.key) && valueRegex.matches(it.value) }
}
