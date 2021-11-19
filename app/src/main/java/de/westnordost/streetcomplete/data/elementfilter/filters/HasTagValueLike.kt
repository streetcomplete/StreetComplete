package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** key ~ val(ue)? */
class HasTagValueLike(val key: String, val value: String) :  ElementFilter {
    private val regex = RegexOrSet.from(value)

    override fun toString() = "$key ~ $value"
    override fun matches(obj: Element) = obj.tags[key]?.let { regex.matches(it) } ?: false
}
