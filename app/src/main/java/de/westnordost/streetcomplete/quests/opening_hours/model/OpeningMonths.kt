package de.westnordost.streetcomplete.quests.opening_hours.model

import java.text.DateFormatSymbols
import java.util.*

class OpeningMonths(var months: CircularSection, var weekdaysClusters: List<List<OpeningWeekdays>>){

    private val isWholeYear: Boolean
        get() {
            val aYear = NumberSystem(0, MAX_MONTH_INDEX)
            return aYear.complemented(listOf(months)).isEmpty()
        }

    override fun toString(): String {
        // the US locale is important here as this is the OSM format for dates
        val monthsSymbols = DateFormatSymbols.getInstance(Locale.US).shortMonths
        val months = if(isWholeYear) "" else months.toStringUsing(monthsSymbols, "-") + ": "

        return weekdaysClusters.joinToString("; ") { weekdaysCluster ->
            weekdaysCluster.joinToString(", ") { openingWeekdays ->
                val weekdays = openingWeekdays.weekdays.toString()
                val times = openingWeekdays.timeRanges.joinToString(",")
                months + weekdays + " " + times
            }
        }
    }

    fun containsSelfIntersectingOpeningWeekdays(): Boolean {
        for (weekdaysCluster in weekdaysClusters) {
            for (openingWeekdays in weekdaysCluster) {
                if (openingWeekdays.isSelfIntersecting()) return true
            }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpeningMonths) return false

        return months == other.months && weekdaysClusters == other.weekdaysClusters
    }

    override fun hashCode() = 31 * months.hashCode() + weekdaysClusters.hashCode()

    companion object {
        val MAX_MONTH_INDEX = 11
    }
}
