package de.westnordost.streetcomplete.ktx

import android.view.animation.Animation

inline fun Animation.setAnimationListener(
    crossinline onEnd: (animation: Animation) -> Unit = {},
    crossinline onStart: (animation: Animation) -> Unit = {},
    crossinline onRepeat: (animation: Animation) -> Unit = {}
): Animation.AnimationListener {
    val listener = object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) { onEnd(animation) }
        override fun onAnimationStart(animation: Animation) { onStart(animation) }
        override fun onAnimationRepeat(animation: Animation) { onRepeat(animation) }
    }
    this.setAnimationListener(listener)
    return listener
}