package de.westnordost.streetcomplete.util.locale

import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * A monetary amount, represented exactly as an integer number of units and an integer number
 * of subunits (e.g. euros and cents), to avoid floating point rounding issues.
 *
 * How many digits [subunits] may have is not stored here (it depends on the currency, see
 * [CurrencyFormatElements.decimalDigits]) - the caller must keep track of that.
 */
@Serializable
data class CurrencyAmount(val units: Long, val subunits: Long) {

    fun toDouble(decimalDigits: Int): Double =
        units + subunits / 10.0.pow(decimalDigits)

    /**
     * Returns a string representation of this currency amount, formatting the [subunits]
     */
    fun toString(decimalDigits: Int): String =
        "$units.${subunits.toString().padStart(decimalDigits, '0')}"

    companion object {
        fun fromDouble(value: Double, decimalDigits: Int): CurrencyAmount {
            val factor = 10.0.pow(decimalDigits).roundToLong().coerceAtLeast(1)
            val scaled = (value.absoluteValue * factor).roundToLong()
            return CurrencyAmount(scaled / factor, scaled % factor)
        }
    }
}
