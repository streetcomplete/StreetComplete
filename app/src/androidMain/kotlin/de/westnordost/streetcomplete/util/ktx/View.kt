package de.westnordost.streetcomplete.util.ktx

import android.graphics.Point
import android.view.View

fun View.getLocationInWindow(): Point {
    val pos = IntArray(2)
    getLocationInWindow(pos)
    return Point(pos[0], pos[1])
}
