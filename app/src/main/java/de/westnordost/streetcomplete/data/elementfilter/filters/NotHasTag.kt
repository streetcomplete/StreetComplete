package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

/** key != value */
class NotHasTag(val key: String, val value: String) : ElementFilter {
    override fun toString() = "$key != $value"
    override fun matches(obj: Element) = obj.tags[key] != value
}
