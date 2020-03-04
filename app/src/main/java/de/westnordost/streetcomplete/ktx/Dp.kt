package de.westnordost.streetcomplete.ktx

import android.content.Context

fun Float.toDp(context: Context): Float {
    return this / context.resources.displayMetrics.density
}

fun Float.toPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}
