package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import java.util.*

abstract class CompareElementAge(private val dateFilter: DateFilter) : ElementFilter {
    val date: Date get() = dateFilter.date

    override fun toOverpassQLString() =
        "(if: date(timestamp()) " + operator + " date('" + date.toCheckDateString() + "'))"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val timestampEdited = obj?.timestampEdited ?: return false
        return compareTo(Date(timestampEdited))
    }

    abstract fun compareTo(tagValue: Date): Boolean
    abstract val operator: String
}
