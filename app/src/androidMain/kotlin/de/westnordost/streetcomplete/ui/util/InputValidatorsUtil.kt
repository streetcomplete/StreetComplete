package de.westnordost.streetcomplete.ui.util

/** Check if the [string] is a(n optional) decimal number with the specified number of
 *  [maxIntegerDigits] and [maxFractionDigits]. */
fun isOnlyDecimalDigits(
    string: String,
    decimalSeparator: Char,
    maxIntegerDigits: Int,
    maxFractionDigits: Int
): Boolean {
    if (!string.all { it.isDigit() || it == decimalSeparator }) return false
    val texts = string.split(decimalSeparator)
    if (texts.size > 2 || texts.isEmpty()) return false
    if (texts[0].length > maxIntegerDigits) return false
    if (texts.size > 1 && texts[1].length > maxFractionDigits) return false
    return true
}

/** Check if the [string] is a valid feet input with a maximum of [maxFeetDigits] digits */
fun isValidFeetInput(string: String, maxFeetDigits: Int): Boolean {
    if (!string.all { it.isDigit() }) return false
    if (string.length > maxFeetDigits) return false
    return true
}

/** Check if the [string] is a valid inch input */
fun isValidInchesInput(string: String): Boolean {
    if (!string.all { it.isDigit() }) return false
    val value = string.toInt()
    return value >= 0 && value < 12
}
