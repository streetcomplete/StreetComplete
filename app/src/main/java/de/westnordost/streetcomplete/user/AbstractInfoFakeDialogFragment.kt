package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.Transforms
import de.westnordost.streetcomplete.util.animateFrom
import de.westnordost.streetcomplete.util.animateTo
import de.westnordost.streetcomplete.util.applyTransforms

/**  It is not a real dialog because a real dialog has its own window, or in other words, has a
 *  different root view than the rest of the UI. However, for the calculation to animate the icon
 *  from another view to the position in the "dialog", there must be a common root view.*/
abstract class AbstractInfoFakeDialogFragment(layoutId: Int) : Fragment(layoutId) {

    /** View from which the title image view is animated from (and back on dismissal)*/
    private var sharedTitleView: View? = null

    var isShowing: Boolean = false
        private set

    // need to keep the animators here to be able to clear them on cancel
    private val currentAnimators: MutableList<ViewPropertyAnimator> = mutableListOf()

    private lateinit var dialogAndBackgroundContainer: ViewGroup
    private lateinit var dialogBackground: View
    private lateinit var dialogContentContainer: ViewGroup
    private lateinit var dialogBubbleBackground: View
    private lateinit var titleView: View

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogAndBackgroundContainer = view.findViewById(R.id.dialogAndBackgroundContainer)
        dialogBackground = view.findViewById(R.id.dialogBackground)
        titleView = view.findViewById(R.id.titleView)
        dialogContentContainer = view.findViewById(R.id.dialogContentContainer)
        dialogBubbleBackground = view.findViewById(R.id.dialogBubbleBackground)

        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedTitleView = null
        clearAnimators()
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    open fun dismiss(): Boolean {
        if (currentAnimators.isNotEmpty()) return false
        isShowing = false
        animateOut(sharedTitleView)
        return true
    }

    protected fun show(sharedView: View): Boolean {
        if (currentAnimators.isNotEmpty()) return false
        isShowing = true
        this.sharedTitleView = sharedView
        animateIn(sharedView)
        return true
    }

    /* ----------------------------------- Animating in and out --------------------------------- */

    private fun animateIn(sharedView: View) {
        dialogAndBackgroundContainer.visibility = View.VISIBLE

        currentAnimators.addAll(
            createDialogPopInAnimations() + listOf(
                createTitleImageFlingInAnimation(sharedView),
                createFadeInBackgroundAnimation()
            )
        )
        currentAnimators.forEach { it.start() }
    }

    private fun animateOut(sharedView: View?) {
        currentAnimators.addAll(createDialogPopOutAnimations())
        if (sharedView != null) currentAnimators.add(createTitleImageFlingOutAnimation(sharedView))
        currentAnimators.add(createFadeOutBackgroundAnimation())
        currentAnimators.forEach { it.start() }
    }

    private fun createFadeInBackgroundAnimation(): ViewPropertyAnimator {
        dialogBackground.alpha = 0f
        return dialogBackground.animate()
            .alpha(1f)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { currentAnimators.clear() }
    }

    private fun createFadeOutBackgroundAnimation(): ViewPropertyAnimator {
        return dialogBackground.animate()
            .alpha(0f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                dialogAndBackgroundContainer.visibility = View.INVISIBLE
                currentAnimators.clear()
            }
    }

    private fun createTitleImageFlingInAnimation(sourceView: View): ViewPropertyAnimator {
        sourceView.visibility = View.INVISIBLE
        val root = sourceView.rootView as ViewGroup
        titleView.applyTransforms(Transforms.IDENTITY)
        return titleView.animateFrom(sourceView, root)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(OvershootInterpolator())
    }

    private fun createTitleImageFlingOutAnimation(targetView: View): ViewPropertyAnimator {
        val root = targetView.rootView as ViewGroup
        return titleView.animateTo(targetView, root)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                targetView.visibility = View.VISIBLE
                sharedTitleView = null
            }
    }

    private fun createDialogPopInAnimations(): List<ViewPropertyAnimator> {
        return listOf(dialogContentContainer, dialogBubbleBackground).map {
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
    }

    private fun createDialogPopOutAnimations(): List<ViewPropertyAnimator> {
        return listOf(dialogContentContainer, dialogBubbleBackground).map {
            it.animate()
                .alpha(0f)
                .scaleX(0.5f).scaleY(0.5f)
                .translationYBy(it.height * 0.2f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator())
        }
    }

    private fun clearAnimators() {
        for (anim in currentAnimators) {
            anim.cancel()
        }
        currentAnimators.clear()
    }

    companion object {
        const val ANIMATION_TIME_IN_MS = 600L
        const val ANIMATION_TIME_OUT_MS = 300L
    }
}
