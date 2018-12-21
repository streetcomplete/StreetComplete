package de.westnordost.streetcomplete.xt

import android.view.View
import android.view.ViewGroup

fun View.updateLayoutParams(block: ViewGroup.LayoutParams.() -> Unit) {
    layoutParams = layoutParams.apply(block)
}
