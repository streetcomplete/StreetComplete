package de.westnordost.streetcomplete.view.inputfilter

import android.text.InputFilter
import android.text.Spanned

/** Input filter that only allows a certain range of integers to be input */
class IntRangeInputValidator(private val range: IntRange) : InputFilter {
    override fun filter(
        source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int,
    ): CharSequence? {
        val builder = StringBuilder(dest)
        val replaceText = source.subSequence(start, end).toString()
        val newText = builder.replace(dstart, dend, replaceText).toString()
        return if (range.contains(newText.toIntOrNull())) null else ""
    }
}
