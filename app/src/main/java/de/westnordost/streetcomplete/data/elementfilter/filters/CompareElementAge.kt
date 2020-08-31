package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import java.util.*

abstract class CompareElementAge(private val dateFilter: DateFilter) : ElementFilter {
    val date: Date get() = dateFilter.date

    override fun toOverpassQLString() =
        "(if: date(timestamp()) " + operator + " date('" + date.toCheckDateString() + "'))"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val dateElementEdited = obj?.dateEdited ?: return false
        return compareTo(dateElementEdited)
    }

    abstract fun compareTo(tagValue: Date): Boolean
    abstract val operator: String
}