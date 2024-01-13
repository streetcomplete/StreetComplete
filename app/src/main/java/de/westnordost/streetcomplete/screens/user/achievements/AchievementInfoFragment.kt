package de.westnordost.streetcomplete.screens.user.achievements

import android.animation.LayoutTransition
import android.animation.LayoutTransition.APPEARING
import android.animation.LayoutTransition.DISAPPEARING
import android.animation.TimeAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.databinding.FragmentAchievementInfoBinding
import de.westnordost.streetcomplete.screens.user.links.LinksAdapter
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.Transforms
import de.westnordost.streetcomplete.view.ViewPropertyAnimatorsPlayer
import de.westnordost.streetcomplete.view.animateFrom
import de.westnordost.streetcomplete.view.animateTo
import de.westnordost.streetcomplete.view.applyTransforms

/** Shows details for a certain level of one achievement as a fake-dialog.
 *  There are two modes:
 *
 *  1. Show details of a newly achieved achievement. The achievement icon animates in in a fancy way
 *     and some shining animation is played. The unlocked links are shown.
 *
 *  2. Show details of an already achieved achievement. The achievement icon animates from another
 *     view to its current position, no shining animation is played. Also, the unlocked links are
 *     not shown because they can be looked at in the links screen.
 *
 *  It is not a real dialog because a real dialog has its own window, or in other words, has a
 *  different root view than the rest of the UI. However, for the calculation to animate the icon
 *  from another view to the position in the "dialog", there must be a common root view.
 *  */
class AchievementInfoFragment : Fragment(R.layout.fragment_achievement_info) {

    private val binding by viewBinding(FragmentAchievementInfoBinding::bind)

    /** View from which the achievement icon is animated from (and back on dismissal)*/
    private var achievementIconBubble: View? = null

    private var animatorsPlayer: ViewPropertyAnimatorsPlayer? = null
    private var shineAnimation: TimeAnimator? = null

    private val layoutTransition: LayoutTransition = LayoutTransition()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            dismiss()
        }
    }

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    init {
        layoutTransition.disableTransitionType(APPEARING)
        layoutTransition.disableTransitionType(DISAPPEARING)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dialogAndBackgroundContainer.setOnClickListener { dismiss() }
        // in order to not show the scroll indicators
        binding.unlockedLinksList.isNestedScrollingEnabled = false
        binding.unlockedLinksList.layoutManager = object : LinearLayoutManager(requireContext(), VERTICAL, false) {
            override fun canScrollVertically() = false
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        achievementIconBubble = null
        animatorsPlayer?.cancel()
        shineAnimation?.cancel()
        shineAnimation = null
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    /** Show as details of a tapped view */
    fun show(achievement: Achievement, level: Int, achievementBubbleView: View): Boolean {
        if (animatorsPlayer != null) return false
        backPressedCallback.isEnabled = true
        this.achievementIconBubble = achievementBubbleView

        bind(achievement, level, false)
        animateInFromView(achievementBubbleView)
        return true
    }

    /** Show as new achievement achieved/unlocked */
    fun showNew(achievement: Achievement, level: Int): Boolean {
        if (animatorsPlayer != null) return false
        backPressedCallback.isEnabled = true

        bind(achievement, level, true)
        animateIn()
        return true
    }

    fun dismiss(): Boolean {
        if (animatorsPlayer != null) return false
        backPressedCallback.isEnabled = false
        animateOut(achievementIconBubble)
        return true
    }

    /* ----------------------------------- Animating in and out --------------------------------- */

    private fun bind(achievement: Achievement, level: Int, showLinks: Boolean) {
        binding.achievementIconView.icon = context?.getDrawable(achievement.icon)
        binding.achievementIconView.level = level
        binding.achievementTitleText.setText(achievement.title)

        binding.achievementDescriptionText.isGone = achievement.description == null
        if (achievement.description != null) {
            val arg = achievement.getPointThreshold(level)
            binding.achievementDescriptionText.text = resources.getString(achievement.description, arg)
        } else {
            binding.achievementDescriptionText.text = ""
        }

        val unlockedLinks = achievement.unlockedLinks[level].orEmpty()
        val hasNoUnlockedLinks = unlockedLinks.isEmpty() || !showLinks
        binding.unlockedLinkTitleText.isGone = hasNoUnlockedLinks
        binding.unlockedLinksList.isGone = hasNoUnlockedLinks
        if (hasNoUnlockedLinks) {
            binding.unlockedLinksList.adapter = null
        } else {
            binding.unlockedLinkTitleText.setText(
                if (unlockedLinks.size == 1) {
                    R.string.achievements_unlocked_link
                } else {
                    R.string.achievements_unlocked_links
                }
            )
            binding.unlockedLinksList.adapter = LinksAdapter(unlockedLinks, ::openUri)
        }
    }

    private fun animateIn() {
        binding.dialogAndBackgroundContainer.visibility = View.VISIBLE

        shineAnimation?.cancel()
        val anim = TimeAnimator()
        anim.setTimeListener { _, _, deltaTime ->
            binding.shineView1.rotation += deltaTime / 50f
            binding.shineView2.rotation -= deltaTime / 100f
        }
        anim.start()
        shineAnimation = anim

        playAll(
            *createShineFadeInAnimations().toTypedArray(),
            *createDialogPopInAnimations(DIALOG_APPEAR_DELAY_IN_MS).toTypedArray(),
            createFadeInBackgroundAnimation(),
            createAchievementIconPopInAnimation()
        )
    }

    private fun animateInFromView(questBubbleView: View) {
        questBubbleView.visibility = View.INVISIBLE
        binding.dialogAndBackgroundContainer.visibility = View.VISIBLE

        binding.shineView1.visibility = View.GONE
        binding.shineView2.visibility = View.GONE

        playAll(
            *createDialogPopInAnimations().toTypedArray(),
            createFadeInBackgroundAnimation(),
            createAchievementIconFlingInAnimation(questBubbleView)
        )
    }

    private fun animateOut(questBubbleView: View?) {
        binding.dialogContainer.layoutTransition = null

        val iconAnimator = if (questBubbleView != null) {
            createAchievementIconFlingOutAnimation(questBubbleView)
        } else {
            createAchievementIconPopOutAnimation()
        }

        playAll(
            *createShineFadeOutAnimations().toTypedArray(),
            *createDialogPopOutAnimations().toTypedArray(),
            createFadeOutBackgroundAnimation(),
            iconAnimator
        )
    }

    private fun createAchievementIconFlingInAnimation(sourceView: View): ViewPropertyAnimator =
        binding.achievementIconView.let {
            sourceView.visibility = View.INVISIBLE
            it.applyTransforms(Transforms.IDENTITY)
            it.alpha = 1f
            it.animateFrom(sourceView)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(OvershootInterpolator())
        }

    private fun createAchievementIconFlingOutAnimation(targetView: View): ViewPropertyAnimator =
        binding.achievementIconView.animateTo(targetView)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                targetView.visibility = View.VISIBLE
                achievementIconBubble = null
            }

    private fun createShineFadeInAnimations(): List<ViewPropertyAnimator> =
        listOf(binding.shineView1, binding.shineView2).map {
            it.visibility = View.VISIBLE
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(DecelerateInterpolator())
        }

    private fun createShineFadeOutAnimations(): List<ViewPropertyAnimator> =
        listOf(binding.shineView1, binding.shineView2).map {
            it.animate()
                .alpha(0f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    shineAnimation?.cancel()
                    shineAnimation = null
                    it.visibility = View.GONE
                }
        }

    private fun createAchievementIconPopInAnimation(): ViewPropertyAnimator =
        binding.achievementIconView.let {
            it.alpha = 0f
            it.scaleX = 0f
            it.scaleY = 0f
            it.rotationY = -180f
            it.animate()
                .alpha(1f)
                .scaleX(1f).scaleY(1f)
                .rotationY(360f)
                .setDuration(ANIMATION_TIME_NEW_ACHIEVEMENT_IN_MS)
                .setInterpolator(DecelerateInterpolator())
        }

    private fun createAchievementIconPopOutAnimation(): ViewPropertyAnimator =
        binding.achievementIconView.animate()
            .alpha(0f)
            .scaleX(0.5f).scaleY(0.5f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())

    private fun createDialogPopInAnimations(startDelay: Long = 0): List<ViewPropertyAnimator> =
        listOf(binding.dialogContentContainer, binding.dialogBubbleBackground).map {
            it.alpha = 0f
            it.scaleX = 0.5f
            it.scaleY = 0.5f
            it.translationY = 0f
            /* For the "show new achievement" mode, only the icon is shown first and only after a
             * delay, the dialog with the description etc.
             * This icon is in the center at first and should animate up while the dialog becomes
             * visible. This movement is solved via a (default) layout transition here for which the
             * APPEARING transition type is disabled because we animate the alpha ourselves. */
            it.isGone = startDelay > 0
            it.animate()
                .setStartDelay(startDelay)
                .withStartAction {
                    if (startDelay > 0) {
                        binding.dialogContainer.layoutTransition = layoutTransition
                        it.visibility = View.VISIBLE
                    }
                }
                .alpha(1f)
                .scaleX(1f).scaleY(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(OvershootInterpolator())
        }

    private fun createDialogPopOutAnimations(): List<ViewPropertyAnimator> =
        listOf(binding.dialogContentContainer, binding.dialogBubbleBackground).map {
            it.animate()
                .alpha(0f)
                .setStartDelay(0)
                .scaleX(0.5f).scaleY(0.5f)
                .translationYBy(it.height * 0.2f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator())
        }

    private fun createFadeInBackgroundAnimation(): ViewPropertyAnimator =
        binding.dialogBackground.let {
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(DecelerateInterpolator())
        }

    private fun createFadeOutBackgroundAnimation(): ViewPropertyAnimator =
        binding.dialogBackground.animate()
            .alpha(0f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                binding.dialogAndBackgroundContainer.visibility = View.INVISIBLE
            }

    private fun playAll(vararg animators: ViewPropertyAnimator) {
        animatorsPlayer = ViewPropertyAnimatorsPlayer(animators.toMutableList()).also {
            it.onEnd = { animatorsPlayer = null }
            it.start()
        }
    }

    companion object {
        const val ANIMATION_TIME_NEW_ACHIEVEMENT_IN_MS = 1000L
        const val ANIMATION_TIME_IN_MS = 400L
        const val DIALOG_APPEAR_DELAY_IN_MS = 1600L
        const val ANIMATION_TIME_OUT_MS = 300L
    }
}
