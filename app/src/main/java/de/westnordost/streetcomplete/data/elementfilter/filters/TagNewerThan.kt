package de.westnordost.streetcomplete.data.elementfilter.filters

import java.time.LocalDate

/** key newer 2000-02-02 / key newer -8 years */
class TagNewerThan(key: String, dateFilter: DateFilter) : CompareTagAge(key, dateFilter) {
    override fun compareTo(tagValue: LocalDate) = tagValue > date
    override val operator = ">"
}
