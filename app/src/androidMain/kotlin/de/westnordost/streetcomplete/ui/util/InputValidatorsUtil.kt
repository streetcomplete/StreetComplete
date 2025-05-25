package de.westnordost.streetcomplete.ui.util

/** Only accept inputs that are numbers with the max number of digits before and after the decimal point */
fun onlyDecimalDigits(string: String, beforeDecimalPoint: Int, afterDecimalPoint: Int): Boolean {
    if (!string.all { it.isDigit() || it == '.' || it == ',' }) return false;
    val texts = string.split(',', '.')
    if (texts.size > 2 || texts.isEmpty()) return false
    val before = texts[0]
    val after = if (texts.size > 1) texts[1] else ""
    return string.toDoubleOrNull() != null && after.length <= afterDecimalPoint && before.length <= beforeDecimalPoint
}

/** Check if the input is a valid feet input with a maximum of maxFeetDigits digits */
fun validFeetInput(string: String, maxFeetDigits: Int): Boolean {
    return string.toIntOrNull() != null && string.length <= maxFeetDigits
}

/** Check if the input is a valid inch input */
fun validInchInput(string: String): Boolean {
    val value = string.toIntOrNull()
    return value != null && value >= 0 && value < 12
}
