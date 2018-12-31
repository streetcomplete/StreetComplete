package de.westnordost.streetcomplete.xt

import android.view.View
import android.view.ViewGroup
import de.westnordost.streetcomplete.util.ViewUtils

fun View.updateLayoutParams(block: ViewGroup.LayoutParams.() -> Unit) {
    layoutParams = layoutParams.apply(block)
}

fun View.postOnLayout(callback:() -> Unit) {
    ViewUtils.postOnLayout(this, callback)
}
