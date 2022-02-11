package de.westnordost.streetcomplete.view.inputfilter

import android.text.InputFilter
import android.text.Spanned

class MaxDecimalsInputValidator(private val maxDecimals: Int) : InputFilter {
    override fun filter(
        source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int,
    ): CharSequence? {
        val builder = StringBuilder(dest)
        val replaceText = source.subSequence(start, end).toString()
        val newText = builder.replace(dstart, dend, replaceText).toString()
        val decimals = newText
            .replace(",", ".")
            .substringAfterLast(".", "")
        return if (decimals.length <= maxDecimals) null else ""
    }
}
