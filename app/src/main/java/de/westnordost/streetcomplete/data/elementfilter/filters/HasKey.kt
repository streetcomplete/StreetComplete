package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** key */
class HasKey(val key: String) : ElementFilter {
    override fun toString() = key
    override fun matches(obj: Element) = obj.tags.containsKey(key)
}
