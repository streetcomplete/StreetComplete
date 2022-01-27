package de.westnordost.streetcomplete.util

import android.animation.Animator
import android.view.ViewPropertyAnimator

/** Plays the given ViewPropertyAnimators and optionally executes an action when all finished. */
class ViewPropertyAnimatorsPlayer(animators: Collection<ViewPropertyAnimator>) {
    private var started: Boolean = false
    private val animators = animators.toMutableList()

    val isAnimating: Boolean get() = started && animators.isNotEmpty()

    var onEnd: (() -> Unit)? = null

    fun start() {
        if (started) return
        started = true
        checkIsEnded()
        // copy for iterating because animators could be removed from the list while iterating
        val animators = ArrayList(animators)
        animators.forEach {
            it.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) { removeAnimator(it) }
                override fun onAnimationCancel(animation: Animator) { removeAnimator(it) }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            it.start()
        }
    }

    fun cancel() {
        // copy for iterating because animators could be removed from the list while iterating
        val animators = ArrayList(animators)
        animators.forEach { it.cancel() }
    }

    private fun removeAnimator(animator: ViewPropertyAnimator) {
        animators.remove(animator)
        checkIsEnded()
    }

    private fun checkIsEnded() {
        if (animators.isEmpty()) {
            onEnd?.invoke()
        }
    }
}
