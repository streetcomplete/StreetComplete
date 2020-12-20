package de.westnordost.streetcomplete.view

import android.widget.TextView
import androidx.annotation.StringRes

/* Same idea here as the Icon class introduced in min API level 23. If the min API level is
   Build.VERSION_CODES_M, usage of this class can be replaced with Icon */

sealed class Text
data class ResText(@StringRes val resId: Int) : Text()
data class CharSequenceText(val text: CharSequence) : Text()

fun TextView.setText(text: Text?) {
    when(text) {
        is ResText -> setText(text.resId)
        is CharSequenceText -> setText(text.text)
        null -> setText("")
    }
}

