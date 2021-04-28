package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ktx.toLocalDate
import java.time.Instant
import java.time.LocalDate

abstract class CompareElementAge(private val dateFilter: DateFilter) : ElementFilter {
    val date: LocalDate get() = dateFilter.date

    override fun toOverpassQLString() =
        "(if: date(timestamp()) " + operator + " date('" + date.toCheckDateString() + "'))"

    override fun toString() = toOverpassQLString()

    override fun matches(obj: Element?): Boolean {
        val timestampEdited = obj?.timestampEdited ?: return false
        return compareTo(Instant.ofEpochMilli(timestampEdited).toLocalDate())
    }

    abstract fun compareTo(tagValue: LocalDate): Boolean
    abstract val operator: String
}
