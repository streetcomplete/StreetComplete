package de.westnordost.streetcomplete.screens.user.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.view.Transforms
import de.westnordost.streetcomplete.view.ViewPropertyAnimatorsPlayer
import de.westnordost.streetcomplete.view.animateFrom
import de.westnordost.streetcomplete.view.animateTo
import de.westnordost.streetcomplete.view.applyTransforms

/**  It is not a real dialog because a real dialog has its own window, or in other words, has a
 *  different root view than the rest of the UI. However, for the calculation to animate the icon
 *  from another view to the position in the "dialog", there must be a common root view.*/
abstract class AbstractInfoFakeDialogFragment(layoutId: Int) : Fragment(layoutId) {

    /** View from which the title image view is animated from (and back on dismissal)*/
    private var sharedTitleView: View? = null

    private var animatorsPlayer: ViewPropertyAnimatorsPlayer? = null

    protected abstract val dialogAndBackgroundContainer: ViewGroup
    protected abstract val dialogBackground: View
    protected abstract val dialogContentContainer: ViewGroup
    protected abstract val dialogBubbleBackground: View
    protected abstract val titleView: View

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            dismiss()
        }
    }

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedTitleView = null
        animatorsPlayer?.cancel()
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    open fun dismiss(): Boolean {
        if (animatorsPlayer != null) return false
        backPressedCallback.isEnabled = false
        animateOut(sharedTitleView)
        return true
    }

    protected fun show(sharedView: View): Boolean {
        if (animatorsPlayer != null) return false
        backPressedCallback.isEnabled = true
        this.sharedTitleView = sharedView
        animateIn(sharedView)
        return true
    }

    /* ----------------------------------- Animating in and out --------------------------------- */

    private fun animateIn(sharedView: View) {
        dialogAndBackgroundContainer.visibility = View.VISIBLE

        playAll(
            createDialogPopInAnimations() + listOf(
                createTitleImageFlingInAnimation(sharedView),
                createFadeInBackgroundAnimation()
            )
        )
    }

    private fun animateOut(sharedView: View?) {
        val animators = mutableListOf<ViewPropertyAnimator>()
        animators.addAll(createDialogPopOutAnimations())
        if (sharedView != null) animators.add(createTitleImageFlingOutAnimation(sharedView))
        animators.add(createFadeOutBackgroundAnimation())
        playAll(animators)
    }

    private fun createFadeInBackgroundAnimation(): ViewPropertyAnimator {
        dialogBackground.alpha = 0f
        return dialogBackground.animate()
            .alpha(1f)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(DecelerateInterpolator())
    }

    private fun createFadeOutBackgroundAnimation(): ViewPropertyAnimator =
        dialogBackground.animate()
            .alpha(0f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                dialogAndBackgroundContainer.visibility = View.INVISIBLE
            }

    private fun createTitleImageFlingInAnimation(sourceView: View): ViewPropertyAnimator {
        sourceView.visibility = View.INVISIBLE
        titleView.applyTransforms(Transforms.IDENTITY)
        return titleView.animateFrom(sourceView)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(OvershootInterpolator())
    }

    private fun createTitleImageFlingOutAnimation(targetView: View): ViewPropertyAnimator =
        titleView.animateTo(targetView)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                targetView.visibility = View.VISIBLE
                sharedTitleView = null
            }

    private fun createDialogPopInAnimations(): List<ViewPropertyAnimator> =
        listOf(dialogContentContainer, dialogBubbleBackground).map {
            it.alpha = 0f
            it.scaleX = 0.5f
            it.scaleY = 0.5f
            it.translationY = 0f
            it.animate()
                .alpha(1f)
                .scaleX(1f).scaleY(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(OvershootInterpolator())
        }

    private fun createDialogPopOutAnimations(): List<ViewPropertyAnimator> =
        listOf(dialogContentContainer, dialogBubbleBackground).map {
            it.animate()
                .alpha(0f)
                .scaleX(0.5f).scaleY(0.5f)
                .translationYBy(it.height * 0.2f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator())
        }

    private fun playAll(animators: Collection<ViewPropertyAnimator>) {
        animatorsPlayer = ViewPropertyAnimatorsPlayer(animators.toMutableList()).also {
            it.onEnd = { animatorsPlayer = null }
            it.start()
        }
    }

    companion object {
        const val ANIMATION_TIME_IN_MS = 600L
        const val ANIMATION_TIME_OUT_MS = 300L
    }
}
