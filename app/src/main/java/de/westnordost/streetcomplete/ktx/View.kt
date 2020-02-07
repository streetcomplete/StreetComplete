package de.westnordost.streetcomplete.ktx

import android.graphics.Point
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

suspend fun View.awaitLayout() = suspendCoroutine<Unit> { cont -> doOnLayout { cont.resume(Unit) }}
suspend fun View.awaitPreDraw() = suspendCoroutine<Unit> { cont -> doOnPreDraw { cont.resume(Unit) }}

fun View.getLocationInWindow(): Point {
    val mapPosition = IntArray(2)
    getLocationInWindow(mapPosition)
    return Point(mapPosition[0], mapPosition[1])
}
