package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary

/** key ~ val(ue)? */
class HasTagValueLike(val key: String, value: String) :  ElementFilter {
    val value = value.toRegex()

    override fun toOverpassQLString() =
        "[" + key.quoteIfNecessary() + " ~ " + "^(${value.pattern})$".quoteIfNecessary() + "]"

    override fun toString() = toOverpassQLString()
    override fun matches(obj: Element?) = obj?.tags?.get(key)?.matches(value) ?: false
}