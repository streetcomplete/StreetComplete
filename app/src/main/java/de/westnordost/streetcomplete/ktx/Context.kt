package de.westnordost.streetcomplete.ktx

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.getSystemService

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun View.showKeyboard(): Boolean? =
    context?.inputMethodManager?.showSoftInput(this, SHOW_IMPLICIT)

fun View.hideKeyboard(): Boolean? =
    context?.inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)

private val Context.inputMethodManager get() = getSystemService<InputMethodManager>()
