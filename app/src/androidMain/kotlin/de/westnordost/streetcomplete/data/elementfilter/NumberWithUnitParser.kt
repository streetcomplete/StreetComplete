package de.westnordost.streetcomplete.data.elementfilter

fun String.withOptionalUnitToDoubleOrNull(): Double? {
    if (isEmpty()) return null
    if (!first().isDigit() && first() != '.') return null

    if (!last().isLetter() && last() != '"' && last() != '\'') return toDoubleOrNull()

    val withUnitResult = withUnitRegex.matchEntire(this)
    if (withUnitResult != null) {
        val (value, unit) = withUnitResult.destructured
        val v = value.toDoubleOrNull() ?: return null
        val factor = toStandardUnitsFactor(unit) ?: return null
        return v * factor
    }

    val feetInchResult = feetInchRegex.matchEntire(this)
    if (feetInchResult != null) {
        val (feet, inches) = feetInchResult.destructured
        return feet.toInt() * toStandardUnitsFactor("ft")!! +
            inches.toInt() * toStandardUnitsFactor("in")!!
    }

    return null
}

private val feetInchRegex = Regex("([0-9]+)\\s*(?:'|ft)\\s*([0-9]+)\\s*(?:\"|in)")
private val withUnitRegex = Regex("([0-9]+|[0-9]*\\.[0-9]+)\\s*([a-z/'\"]+)")

private fun toStandardUnitsFactor(unit: String): Double? = when (unit) {
    // speed: to kilometers per hour
    "km/h", "kph" -> 1.0
    "mph" -> 1.609344
    // width/length/height: to meters
    "m" -> 1.0
    "mm" -> 0.001
    "cm" -> 0.01
    "km" -> 1000.0
    "ft", "'" -> 0.3048
    "in", "\"" -> 0.0254
    "yd", "yds" -> 0.9144
    // weight: to tonnes
    "t" -> 1.0
    "kg" -> 0.001
    "st" -> 0.90718474 // short tons
    "lt" -> 1.0160469 // long tons
    "lb", "lbs" -> 0.00045359237
    "cwt" -> 0.05080234544 // imperial (=long) hundredweight. short cwt is not in use in road traffic
    else -> null
}
