package de.westnordost.streetcomplete.ktx

import android.text.method.DigitsKeyListener
import android.widget.EditText

/* Workaround for an Android bug that it assumes the decimal separator to always be the "."
   for EditTexts with inputType "numberDecimal", independent of Locale. See
   https://issuetracker.google.com/issues/36907764 .

   Affected Android versions are all versions till (exclusive) Android Oreo. */

fun EditText.allowOnlyNumbers() {
    keyListener = DigitsKeyListener.getInstance("0123456789,.")
}

val EditText.numberOrNull get() = text.toString().trim().replace(",", ".").toDoubleOrNull()
