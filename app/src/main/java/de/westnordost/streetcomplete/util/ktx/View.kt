package de.westnordost.streetcomplete.util.ktx

import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.Insets
import androidx.core.os.postDelayed
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

suspend fun View.awaitLayout() {
    if (!isLaidOut || isLayoutRequested) {
        awaitNextLayout()
    }
}

suspend fun View.awaitNextLayout() = suspendCancellableCoroutine { cont ->
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            v?.removeOnLayoutChangeListener(this)
            cont.resume(Unit)
        }
    }
    cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
    addOnLayoutChangeListener(listener)
}

suspend fun View.awaitPreDraw() = suspendCancellableCoroutine { cont ->
    val listener = doOnPreDraw { cont.resume(Unit) }
    cont.invokeOnCancellation { listener.removeListener() }
}

fun View.getLocationInWindow(): Point {
    val pos = IntArray(2)
    getLocationInWindow(pos)
    return Point(pos[0], pos[1])
}

fun View.showTapHint(initialDelay: Long = 300, pressedDelay: Long = 600) {
    handler?.postDelayed(initialDelay) {
        // trick from https://stackoverflow.com/questions/27225014/how-to-trigger-ripple-effect-on-android-lollipop-in-specific-location-within-th
        background?.setHotspot(width / 2f, height / 2f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground?.setHotspot(width / 2f, height / 2f)
        }

        isPressed = true
        handler?.postDelayed(pressedDelay) {
            isPressed = false
        }
    }
}

fun View.setPadding(insets: Insets) {
    setPadding(insets.left, insets.top, insets.right, insets.bottom)
}

fun View.setMargins(insets: Insets) {
    setMargins(insets.left, insets.top, insets.right, insets.bottom)
}

fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> { setMargins(left, top, right, bottom) }
}

fun View.updateMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        setMargins(
            left ?: leftMargin,
            top ?: topMargin,
            right ?: rightMargin,
            bottom ?: bottomMargin
        )
    }
}
