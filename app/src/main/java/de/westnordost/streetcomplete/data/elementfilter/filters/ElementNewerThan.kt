package de.westnordost.streetcomplete.data.elementfilter.filters

import java.util.*

/** newer 2002-11-11 / newer today - 1 year */
class ElementNewerThan(dateFilter: DateFilter) : CompareElementAge(dateFilter) {
    override fun compareTo(tagValue: Date) = tagValue > date
    override val operator = ">"
}