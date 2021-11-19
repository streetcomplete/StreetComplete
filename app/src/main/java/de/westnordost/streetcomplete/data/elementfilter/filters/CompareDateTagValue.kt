package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import java.time.LocalDate

abstract class CompareDateTagValue(val key: String, val dateFilter: DateFilter): ElementFilter {
    val date: LocalDate get() = dateFilter.date

    override fun matches(obj: Element): Boolean {
        val tagValue = obj.tags[key]?.toCheckDate() ?: return false
        return compareTo(tagValue)
    }

    abstract fun compareTo(tagValue: LocalDate): Boolean
}
