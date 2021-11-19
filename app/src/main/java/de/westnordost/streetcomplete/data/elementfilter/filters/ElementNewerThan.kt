package de.westnordost.streetcomplete.data.elementfilter.filters

import java.time.LocalDate

/** newer 2002-11-11 / newer today - 1 year */
class ElementNewerThan(dateFilter: DateFilter) : CompareElementAge(dateFilter) {
    override fun toString() = dateFilter.toString()
    override fun compareTo(tagValue: LocalDate) = tagValue > date
}
