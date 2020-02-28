package de.westnordost.streetcomplete.user

import android.animation.LayoutTransition
import android.animation.LayoutTransition.APPEARING
import android.animation.LayoutTransition.DISAPPEARING
import android.animation.TimeAnimator
import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.achievements.Achievement
import de.westnordost.streetcomplete.data.achievements.Link
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.util.Transforms
import de.westnordost.streetcomplete.util.animateFrom
import de.westnordost.streetcomplete.util.animateTo
import de.westnordost.streetcomplete.util.applyTransforms
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import de.westnordost.streetcomplete.view.ListAdapter
import kotlinx.android.synthetic.main.fragment_achievement_info.*
import kotlinx.android.synthetic.main.row_link_item.view.*

class AchievementInfoFragment : Fragment(R.layout.fragment_achievement_info) {

    private var achievementIconBubble: View? = null

    var isShowing: Boolean = false
        private set

    private val currentAnimators: MutableList<ViewPropertyAnimator> = mutableListOf()
    private var shineAnimation: TimeAnimator? = null

    private val layoutTransition: LayoutTransition = LayoutTransition()

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    init {
        layoutTransition.disableTransitionType(APPEARING)
        layoutTransition.disableTransitionType(DISAPPEARING)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            achievementIconView.outlineProvider = CircularOutlineProvider
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        achievementIconBubble = null
        clearAnimators()
        shineAnimation?.cancel()
        shineAnimation = null
    }


    /* ---------------------------------------- Interface --------------------------------------- */

    /** Show as details of a tapped view */
    fun show(achievement: Achievement, level: Int, achievementBubbleView: View) {
        isShowing = true
        this.achievementIconBubble = achievementBubbleView

        bind(achievement, level, false)
        animateInFromView(achievementBubbleView)
    }

    /** Show as new achievement achieved/unlocked */
    fun showNew(achievement: Achievement, level: Int) {
        isShowing = true

        bind(achievement, level, true)
        animateIn()
    }

    fun dismiss() {
        isShowing = false
        animateOut(achievementIconBubble)
    }

    /* ----------------------------------- Animating in and out --------------------------------- */

    private fun bind(achievement: Achievement, level: Int, showLinks: Boolean) {
        achievementIconView.setImageResource(achievement.icon)
        achievementTitleText.setText(achievement.title)

        val achievementLevel = achievement.levels[level-1]

        if (achievement.description != null) {
            achievementDescriptionText.visibility = View.VISIBLE
            val arg = achievementLevel.threshold
            achievementDescriptionText.text = resources.getString(achievement.description, arg)
        } else {
            achievementDescriptionText.visibility = View.GONE
            achievementDescriptionText.text = ""
        }

        if (achievementLevel.links.isEmpty() || !showLinks) {
            unlockedLinkTitleText.visibility = View.GONE
            unlockedLinksList.visibility = View.GONE
            unlockedLinksList.adapter = null
        } else {
            unlockedLinkTitleText.visibility = View.VISIBLE
            unlockedLinkTitleText.setText(
                if (achievementLevel.links.size == 1) R.string.achievements_unlocked_link
                else R.string.achievements_unlocked_links
            )
            unlockedLinksList.visibility = View.VISIBLE
            unlockedLinksList.adapter = AchievementLinksAdapter(achievementLevel.links)
        }

        if (level > 1) {
            levelText.text = level.toString()
            levelText.visibility = View.VISIBLE
        } else {
            levelText.text = ""
            levelText.visibility = View.GONE
        }
    }

    private fun animateIn() {
        dialogAndBackgroundContainer.visibility = View.VISIBLE

        val anim = TimeAnimator()
        anim.setTimeListener { _, _, deltaTime ->
            shineView1.rotation += deltaTime / 50f
            shineView2.rotation -= deltaTime / 100f
        }
        anim.start()
        shineAnimation = anim

        clearAnimators()

        currentAnimators.addAll(
            createShineFadeInAnimations() +
            createDialogPopInAnimations(DIALOG_APPEAR_DELAY_IN_MS) +
            listOf(
                createFadeInBackgroundAnimation(),
                createAchievementIconPopInAnimation()
            )
        )
        currentAnimators.forEach { it.start() }
    }

    private fun animateInFromView(questBubbleView: View) {
        questBubbleView.visibility = View.INVISIBLE
        dialogAndBackgroundContainer.visibility = View.VISIBLE

        shineView1.visibility = View.GONE
        shineView2.visibility = View.GONE

        clearAnimators()
        currentAnimators.addAll(createDialogPopInAnimations() + listOf(
            createFadeInBackgroundAnimation(),
            createAchievementIconFlingInAnimation(questBubbleView)
        ))
        currentAnimators.forEach { it.start() }
    }

    private fun animateOut(questBubbleView: View?) {

        dialogContainer.layoutTransition = null

        clearAnimators()

        val iconAnimator = if (questBubbleView != null) {
            createAchievementIconFlingOutAnimation(questBubbleView)
        } else {
            createAchievementIconPopOutAnimation()
        }

        currentAnimators.addAll(
            createDialogPopOutAnimations() +
            createShineFadeOutAnimations() +
            listOf(
                createFadeOutBackgroundAnimation(),
                iconAnimator
            )
        )
        currentAnimators.forEach { it.start() }
    }

    private fun createAchievementIconFlingInAnimation(sourceView: View): ViewPropertyAnimator {
        sourceView.visibility = View.INVISIBLE
        val root = sourceView.rootView as ViewGroup
        achievementIconView.applyTransforms(Transforms.IDENTITY)
        return achievementIconView.animateFrom(sourceView, root)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(OvershootInterpolator())
    }

    private fun createAchievementIconFlingOutAnimation(targetView: View): ViewPropertyAnimator {
        val root = targetView.rootView as ViewGroup
        return achievementIconView.animateTo(targetView, root)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                targetView.visibility = View.VISIBLE
                achievementIconBubble = null
            }
    }

    private fun createShineFadeInAnimations(): List<ViewPropertyAnimator> {
        return listOf(shineView1, shineView2).map {
            it.visibility = View.VISIBLE
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(DecelerateInterpolator())
        }
    }

    private fun createShineFadeOutAnimations(): List<ViewPropertyAnimator> {
        return listOf(shineView1, shineView2).map {
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
    }

    private fun createAchievementIconPopInAnimation(): ViewPropertyAnimator {
        achievementIconView.alpha = 0f
        achievementIconView.scaleX = 0f
        achievementIconView.scaleY = 0f
        achievementIconView.rotationY = -180f
        return achievementIconView.animate()
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .rotationY(360f)
            .setDuration(ANIMATION_TIME_NEW_ACHIEVEMENT_IN_MS)
            .setInterpolator(DecelerateInterpolator())
    }

    private fun createAchievementIconPopOutAnimation(): ViewPropertyAnimator {
        return achievementIconView.animate()
            .alpha(0f)
            .scaleX(0.5f).scaleY(0.5f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())
    }

    private fun createDialogPopInAnimations(startDelay: Long = 0): List<ViewPropertyAnimator> {
        return listOf(dialogContentContainer, dialogBubbleBackground).map {
            it.alpha = 0f
            it.scaleX = 0.5f
            it.scaleY = 0.5f
            it.translationY = 0f
            it.visibility = if (startDelay > 0) View.GONE else View.VISIBLE
            it.animate()
                .setStartDelay(startDelay)
                .withStartAction {
                    if (startDelay > 0) {
                        dialogContainer.layoutTransition = layoutTransition
                        it.visibility = View.VISIBLE
                    }
                }
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
                .setStartDelay(0)
                .scaleX(0.5f).scaleY(0.5f)
                .translationYBy(it.height * 0.2f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator())
        }
    }

    private fun createFadeInBackgroundAnimation(): ViewPropertyAnimator {
        dialogBackground.alpha = 0f
        return dialogBackground.animate()
            .alpha(1f)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(DecelerateInterpolator())
    }

    private fun createFadeOutBackgroundAnimation(): ViewPropertyAnimator {
        return dialogBackground.animate()
            .alpha(0f)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                dialogAndBackgroundContainer.visibility = View.INVISIBLE
            }
    }

    private fun clearAnimators() {
        for (anim in currentAnimators) {
            anim.cancel()
        }
        currentAnimators.clear()
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return tryStartActivity(intent)
    }

    companion object {
        const val ANIMATION_TIME_NEW_ACHIEVEMENT_IN_MS = 1000L
        const val ANIMATION_TIME_IN_MS = 500L
        const val DIALOG_APPEAR_DELAY_IN_MS = 2500L
        const val ANIMATION_TIME_OUT_MS = 300L
    }

    private inner class AchievementLinksAdapter(links: List<Link>) : ListAdapter<Link>(links) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_link_item, parent, false))

        inner class ViewHolder(itemView: View) : ListAdapter.ViewHolder<Link>(itemView) {
            override fun onBind(with: Link) {
                if (with.icon != null) {
                    itemView.linkIconImageView.setImageResource(with.icon)
                } else {
                    itemView.linkIconImageView.setImageDrawable(null)
                }
                itemView.linkTitleTextView.text = with.title
                if (with.description != null) {
                    itemView.linkDescriptionTextView.setText(with.description)
                } else {
                    itemView.linkDescriptionTextView.text = ""
                }
                itemView.setOnClickListener { openUrl(with.url) }
            }
        }
    }

}
