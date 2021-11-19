package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ktx.toLocalDate
import java.time.Instant
import java.time.LocalDate

abstract class CompareElementAge(val dateFilter: DateFilter) : ElementFilter {
    val date: LocalDate get() = dateFilter.date

    override fun matches(obj: Element): Boolean {
        return compareTo(Instant.ofEpochMilli(obj.timestampEdited).toLocalDate())
    }

    abstract fun compareTo(tagValue: LocalDate): Boolean
}
