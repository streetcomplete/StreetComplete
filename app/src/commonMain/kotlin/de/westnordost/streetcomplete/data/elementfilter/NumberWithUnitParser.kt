package de.westnordost.streetcomplete.data.elementfilter

fun String.withOptionalUnitToDoubleOrNull(): Double? =
    withOptionalUnitToDoubleOrNull(::toStandardUnitsFactor)

/** Parses a length in meters, accepting only length units. */
fun String.withOptionalLengthUnitToDoubleOrNull(): Double? =
    withOptionalUnitToDoubleOrNull(::toMetersFactor)

private fun String.withOptionalUnitToDoubleOrNull(unitFactor: (String) -> Double?): Double? {
    if (isEmpty()) return null
    if (!first().isDigit() && first() != '.') return null

    if (!last().isLetter() && last() != '"' && last() != '\'') return toDoubleOrNull()

    val withUnitResult = withUnitRegex.matchEntire(this)
    if (withUnitResult != null) {
        val (value, unit) = withUnitResult.destructured
        val v = value.toDoubleOrNull() ?: return null
        val factor = unitFactor(unit) ?: return null
        return v * factor
    }

    val feetInchResult = feetInchRegex.matchEntire(this)
    if (feetInchResult != null) {
        val (feet, inches) = feetInchResult.destructured
        val feetValue = feet.toIntOrNull() ?: return null
        return feetValue * unitFactor("ft")!! +
            inches.toInt() * unitFactor("in")!!
    }

    return null
}

private val feetInchRegex = Regex("([0-9]+)\\s*(?:'|ft)\\s*([0-9]{1,2})\\s*(?:\"|in)")
private val withUnitRegex = Regex("([0-9]+|[0-9]*\\.[0-9]+)\\s*([a-z/'\"]+)")

private fun toStandardUnitsFactor(unit: String): Double? =
    toMetersFactor(unit)
        ?: toKilometersPerHourFactor(unit)
        ?: toTonnesFactor(unit)

private fun toMetersFactor(unit: String): Double? = when (unit) {
    "m" -> 1.0
    "mm" -> 0.001
    "cm" -> 0.01
    "km" -> 1000.0
    "ft", "'" -> 0.3048
    "in", "\"" -> 0.0254
    "yd", "yds" -> 0.9144
    else -> null
}

private fun toKilometersPerHourFactor(unit: String): Double? = when (unit) {
    "km/h", "kph" -> 1.0
    "mph" -> 1.609344
    else -> null
}

private fun toTonnesFactor(unit: String): Double? = when (unit) {
    "t" -> 1.0
    "kg" -> 0.001
    "st" -> 0.90718474 // short tons
    "lt" -> 1.0160469 // long tons
    "lb", "lbs" -> 0.00045359237
    "cwt" -> 0.05080234544 // imperial (=long) hundredweight. short cwt is not in use in road traffic
    else -> null
}
