package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element

abstract class CompareTagValue(val key: String, val value: Float): ElementFilter {

    override fun matches(obj: Element): Boolean {
        val tagValue = obj.tags[key]?.toFloatOrNull() ?: return false
        return compareTo(tagValue)
    }

    abstract fun compareTo(tagValue: Float): Boolean
}
