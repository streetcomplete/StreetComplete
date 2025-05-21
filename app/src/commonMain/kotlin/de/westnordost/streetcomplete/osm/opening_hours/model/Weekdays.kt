package de.westnordost.streetcomplete.osm.opening_hours.model

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import kotlinx.serialization.Serializable
import java.text.DateFormatSymbols
import java.util.Locale

/** A selection of weekdays */
@Serializable
class Weekdays(private val data: BooleanArray = BooleanArray(OSM_ABBR_WEEKDAYS.size)) {

    init {
        require(data.size == OSM_ABBR_WEEKDAYS.size)
    }

    val selection: BooleanArray get() = data.copyOf()

    fun isSelectionEmpty() = data.all { !it }

    override fun toString() = toStringUsing(OSM_ABBR_WEEKDAYS, ",", "-")

    fun toLocalizedString(r: Resources, locale: Locale) =
        toStringUsing(getShortNames(r, locale), ", ", "–")

    fun toStringUsing(names: Array<String>, separator: String, range: String): String {
        val sb = StringBuilder()
        var first = true

        for (section in toCircularSections()) {
            if (!first) {
                sb.append(separator)
            } else {
                first = false
            }

            sb.append(names[section.start])
            if (section.start != section.end) {
                // i.e. Mo-We vs Mo,Tu
                sb.append(if (WEEKDAY_NUMBER_SYSTEM.getSize(section) > 2) range else separator)
                sb.append(names[section.end])
            }
        }

        // the rest (special days). Currently only "PH"
        for (i in WEEKDAY_COUNT until data.size) {
            if (!data[i]) continue

            if (!first) {
                sb.append(separator)
            } else {
                first = false
            }

            sb.append(names[i])
        }

        return sb.toString()
    }

    // section that goes until the end
    fun toCircularSections(): List<CircularSection> {
        val result = mutableListOf<CircularSection>()
        var currentStart: Int? = null
        for (i in 0 until WEEKDAY_COUNT) {
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
            result.add(CircularSection(currentStart, WEEKDAY_COUNT - 1))
        }

        return WEEKDAY_NUMBER_SYSTEM.merged(result)
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
        const val PUBLIC_HOLIDAY = 7
        const val WEEKDAY_COUNT = 7

        private val WEEKDAY_NUMBER_SYSTEM = NumberSystem(0, WEEKDAY_COUNT - 1)

        fun getNames(r: Resources, locale: Locale): Array<String> {
            val symbols = DateFormatSymbols.getInstance(locale)
            val result = symbols.weekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
            result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays)
            return result.requireNoNulls()
        }

        fun getShortNames(r: Resources, locale: Locale): Array<String> {
            val symbols = DateFormatSymbols.getInstance(locale)
            val result = symbols.shortWeekdays.toIso8601Order().copyOf(OSM_ABBR_WEEKDAYS.size)
            result[PUBLIC_HOLIDAY] = r.getString(R.string.quest_openingHours_public_holidays_short)
            return result.requireNoNulls()
        }

        fun getWeekdayIndex(name: String) = OSM_ABBR_WEEKDAYS.indexOf(name)

        private fun Array<String>.toIso8601Order() = Array(7) { this[1 + (it + 1) % 7] }
    }
}
