package de.westnordost.streetcomplete.data.elementfilter.filters

import java.time.LocalDate

/** older 2002-11-11 / older today - 1 year */
class ElementOlderThan(dateFilter: DateFilter) : CompareElementAge(dateFilter) {
    override fun compareTo(tagValue: LocalDate) = tagValue < date
    override val operator = "<"
}
