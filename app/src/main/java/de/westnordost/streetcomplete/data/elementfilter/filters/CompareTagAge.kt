package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.meta.getLastCheckDateKeys
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.ktx.toLocalDate
import java.time.Instant
import java.time.LocalDate

abstract class CompareTagAge(val key: String, val dateFilter: DateFilter) : ElementFilter {
    val date: LocalDate get() = dateFilter.date

    override fun matches(obj: Element): Boolean {
        if (compareTo(Instant.ofEpochMilli(obj.timestampEdited ).toLocalDate())) return true

        return getLastCheckDateKeys(key)
            .mapNotNull { obj.tags[it]?.toCheckDate() }
            .any { compareTo(it) }
    }

    abstract fun compareTo(tagValue: LocalDate): Boolean
}
