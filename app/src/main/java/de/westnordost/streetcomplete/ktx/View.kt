package de.westnordost.streetcomplete.ktx

import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.os.postDelayed
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


fun View.popIn(): ViewPropertyAnimator {
    visibility = View.VISIBLE
    return animate()
        .alpha(1f).scaleX(1f).scaleY(1f)
        .setDuration(100)
        .setInterpolator(DecelerateInterpolator())
        .withEndAction(null)
}

fun View.popOut(): ViewPropertyAnimator {
    return animate()
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

fun View.showTapHint(initialDelay: Long = 300, pressedDelay: Long = 600) {
    handler.postDelayed(initialDelay) {
        // trick from https://stackoverflow.com/questions/27225014/how-to-trigger-ripple-effect-on-android-lollipop-in-specific-location-within-th
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            background?.setHotspot(width / 2f, height / 2f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground?.setHotspot(width / 2f, height / 2f)
        }

        isPressed = true
        handler.postDelayed(pressedDelay) {
            isPressed = false
        }
    }
}
