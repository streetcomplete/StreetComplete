package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlinx.serialization.Serializable
import java.text.DateFormatSymbols
import java.util.Locale

/** A selection of months */
@Serializable
class Months(private val data: BooleanArray = BooleanArray(MONTHS_COUNT)) {

    init {
        require(data.size == MONTHS_COUNT)
    }

    val selection: BooleanArray get() = data.copyOf()

    fun isSelectionEmpty() = data.all { !it }

    override fun toString() = toStringUsing(OSM_ABBR_MONTHS, ",", "-")

    fun toLocalizedString(locale: Locale) =
        toStringUsing(getNames(locale), ", ", "–")

    fun toStringUsing(names: Array<String>, separator: String, range: String): String =
        toCircularSections().joinToString(separator) { section ->
            if (section.start == section.end) {
                names[section.start]
            } else {
                names[section.start] + range + names[section.end]
            }
        }

    fun toCircularSections(): List<CircularSection> {
        val result = mutableListOf<CircularSection>()
        var currentStart: Int? = null
        for (i in 0 until MONTHS_COUNT) {
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
            result.add(CircularSection(currentStart, MONTHS_COUNT - 1))
        }

        return MONTHS_NUMBER_SYSTEM.merged(result)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Months) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode() = data.contentHashCode()

    companion object {

        const val MONTHS_COUNT = 12
        val OSM_ABBR_MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val MONTHS_NUMBER_SYSTEM = NumberSystem(0, MONTHS_COUNT - 1)

        fun getNames(locale: Locale): Array<String> {
            val symbols = DateFormatSymbols.getInstance(locale)
            val result = symbols.months.copyOf(MONTHS_COUNT)
            return result.requireNoNulls()
        }
    }
}
