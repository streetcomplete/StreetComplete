package de.westnordost.streetcomplete.util

import android.text.Editable

class TextChangedWatcher(private val callback: () -> Unit) : DefaultTextWatcher() {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) { callback() }
}
