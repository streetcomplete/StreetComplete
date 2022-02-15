package de.westnordost.streetcomplete.view.inputfilter

import android.text.InputFilter
import android.text.Spanned

class InputValidator(private val validate: (text: String) -> Boolean) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val builder = StringBuilder(dest)
        val replaceText = source.subSequence(start, end).toString()
        val newText = builder.replace(dstart, dend, replaceText).toString()
        return if (validate(newText)) null else ""
    }
}

/** Only accept inputs that are ints in the given range */
fun acceptIntRange(range: IntRange) =
    InputValidator { range.contains(it.toIntOrNull()) }

/** Only accept inputs that are numbers with the max number of digits before and after the decimal point */
fun acceptDecimalDigits(beforeDecimalPoint: Int, afterDecimalPoint: Int) = InputValidator {
    val texts = it.split(',', '.')
    if (texts.size > 2) return@InputValidator false
    val before = texts[0]
    val after = if (texts.size > 1) texts[1] else ""
    return@InputValidator after.length <= afterDecimalPoint && before.length <= beforeDecimalPoint
}

/** Only accept inputs that are ints with the max number of digits */
fun acceptIntDigits(digits: Int) = InputValidator { it.length <= digits }
