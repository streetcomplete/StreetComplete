package de.westnordost.streetcomplete.data.elementfilter.filters

import java.time.LocalDate

/** key older 2000-02-02 / key older -8 years */
class TagOlderThan(key: String, dateFilter: DateFilter) : CompareTagAge(key, dateFilter) {
    override fun compareTo(tagValue: LocalDate) = tagValue < date
    override val operator = "<"
}
