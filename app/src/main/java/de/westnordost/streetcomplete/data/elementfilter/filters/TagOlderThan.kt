package de.westnordost.streetcomplete.data.elementfilter.filters

import java.util.*

/** key older 2000-02-02 / key older -8 years */
class TagOlderThan(key: String, dateFilter: DateFilter) : CompareTagAge(key, dateFilter) {
    override fun compareTo(tagValue: Date) = tagValue < date
    override val operator = "<"
}