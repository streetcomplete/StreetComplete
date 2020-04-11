package de.westnordost.streetcomplete.quests.opening_hours.model

import android.content.res.Resources

import java.text.DateFormatSymbols

import de.westnordost.streetcomplete.R

class Weekdays {

    private val data = BooleanArray(OSM_ABBR_WEEKDAYS.size)

    val selection: BooleanArray get() = data.copyOf()

    constructor()

    constructor(selection: BooleanArray) {
        var i = 0
        while (i < selection.size && i < data.size) {
            data[i] = selection[i]
            ++i
        }
    }

    fun isSelectionEmpty() = data.all { !it }

    override fun toString() = toStringUsing(OSM_ABBR_WEEKDAYS, ",", "-")

    fun toLocalizedString(r: Resources) = toStringUsing(getShortNames(r), ", ", "–")

    fun toStringUsing(names: Array<String>, separator: String, range: String): String {
        val sb = StringBuilder()
        var first = true

        for (section in toCircularSections()) {
            if (!first)
                sb.append(separator)
            else
                first = false

            sb.append(names[section.start])
            if (section.start != section.end) {
                // i.e. Mo-We vs Mo,Tu
                sb.append(if (WEEKDAY_NUMBER_SYSTEM.getSize(section) > 2) range else separator)
                sb.append(names[section.end])
            }
        }

        // the rest (special days). Currently only "PH"
        for (i in 7 until data.size) {
            if (!data[i]) continue

            if (!first)
                sb.append(separator)
            else
                first = false

            sb.append(names[i])
        }

        return sb.toString()
    }

    // section that goes until the end
    private fun toCircularSections(): List<CircularSection> {
        val result = mutableListOf<CircularSection>()
        var currentStart: Int? = null
        for (i in 0..6) {
            if (currentStart == null) {
                if (data[i]) currentStart = i
            } else {
                if (!data[i]) {
                    result.add(CircularSection(currentStart, i - 1))
                    currentStart = null
                }
            }
        }
        if (currentStart != null) {
            result.add(CircularSection(currentStart, 6))
        }

        return WEEKDAY_NUMBER_SYSTEM.merged(result)
    }

    fun intersects(other: Weekdays): Boolean {
        for (i in data.indices) {
            if (data[i] && other.data[i]) return true
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Weekdays) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode() = data.contentHashCode()

    companion object {
        // in ISO 8601 order
        val OSM_ABBR_WEEKDAYS = arrayOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su", "PH")
        private const val PUBLIC_HOLIDAY = 7

        private val WEEKDAY_NUMBER_SYSTEM = NumberSystem(0, 6)

        fun getNames(r: Resources): Array<String> {
            val symbols = DateFormatSymbols.getInstance()
            val result = symbols.weekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
            result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays)
            return result.requireNoNulls()
        }

        fun getShortNames(r: Resources): Array<String> {
            val symbols = DateFormatSymbols.getInstance()
            val result = symbols.shortWeekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
            result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays_short)
            return result.requireNoNulls()
        }

        fun getWeekdayIndex(name: String) = OSM_ABBR_WEEKDAYS.indexOf(name)

        private fun Array<String>.toIso8601Order() = Array(7) { this[1 + (it + 1) % 7] }
    }
}
