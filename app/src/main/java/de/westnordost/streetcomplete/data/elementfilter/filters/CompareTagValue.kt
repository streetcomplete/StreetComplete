package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.quote
import de.westnordost.streetcomplete.data.elementfilter.quoteIfNecessary

abstract class CompareTagValue(val key: String, val value: Float): ElementFilter {
    override fun toOverpassQLString() : String {
        val strVal = if (value - value.toInt() == 0f) value.toInt().toString() else value.toString()
        return "[" + key.quoteIfNecessary() + "](if: number(t[" + key.quote() + "]) " + operator + " " + strVal + ")"
    }

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val tagValue = obj?.tags?.get(key)?.toFloatOrNull() ?: return false
        return compareTo(tagValue)
    }

    abstract fun compareTo(tagValue: Float): Boolean
    abstract val operator: String
}