package de.westnordost.streetcomplete.ktx

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

fun View.popIn() {
    visibility = View.VISIBLE
    animate()
        .alpha(1f).scaleX(1f).scaleY(1f)
        .setDuration(100)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction(null)
}

fun View.popOut() {
    animate()
        .alpha(0f).scaleX(0.5f).scaleY(0.5f)
        .setDuration(100)
        .setInterpolator(AccelerateInterpolator())
        .withEndAction { visibility = View.GONE }
}
