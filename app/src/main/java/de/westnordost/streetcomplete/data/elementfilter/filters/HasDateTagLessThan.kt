package de.westnordost.streetcomplete.data.elementfilter.filters

import java.util.*

/** key < date */
class HasDateTagLessThan(key: String, dateFilter: DateFilter): CompareDateTagValue(key, dateFilter) {
    override val operator = "<"
    override fun compareTo(tagValue: Date) = tagValue < date
}