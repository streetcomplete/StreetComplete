package de.westnordost.streetcomplete.user

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.util.Transforms
import de.westnordost.streetcomplete.util.animateFrom
import de.westnordost.streetcomplete.util.animateTo
import de.westnordost.streetcomplete.util.applyTransforms
import kotlinx.android.synthetic.main.fragment_achievement_info.*

class AchievementInfoFragment : Fragment(R.layout.fragment_achievement_info) {

    private var achievementIconBubble: View? = null

    var isShowing: Boolean = false
        private set

    private val currentAnimators: MutableList<ViewPropertyAnimator> = mutableListOf()

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        achievementIconBubble = null
        clearAnimators()
    }


    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(achievementBubbleView: View, questType: QuestType<*>) {
        isShowing = true
        this.achievementIconBubble = achievementBubbleView

        achievementIconView.setImageResource(questType.icon)
        achievementTitleText.text = resources.getString(questType.title, *Array(10){"…"})

        animateIn(achievementBubbleView)
    }

    fun dismiss() {
        isShowing = false
        achievementIconBubble?.let { animateOut(it) }
    }

    private fun animateIn(questBubbleView: View) {
        questBubbleView.visibility = View.INVISIBLE
        val root = questBubbleView.rootView as ViewGroup

        dialogAndBackgroundContainer.visibility = View.VISIBLE

        achievementIconView.applyTransforms(Transforms.IDENTITY)

        clearAnimators()
        currentAnimators.addAll(listOf(
            dialogBackground.animate()
                .withStartAction {
                    dialogBackground.alpha = 0f
                }
                .alpha(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(DecelerateInterpolator()),
            dialogContainer.animate()
                .withStartAction {
                    dialogContainer.alpha = 0f
                    dialogContainer.scaleX = 0.5f
                    dialogContainer.scaleY = 0.5f
                    dialogContainer.translationY = 0f
                }
                .alpha(1f)
                .scaleX(1f).scaleY(1f)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(OvershootInterpolator()),
            achievementIconView.animateFrom(questBubbleView, root)
                .setDuration(ANIMATION_TIME_IN_MS)
                .setInterpolator(OvershootInterpolator())
        ))
        currentAnimators.forEach { it.start() }
    }

    private fun animateOut(questBubbleView: View) {
        val root = questBubbleView.rootView as ViewGroup

        clearAnimators()
        currentAnimators.addAll(listOf(
            dialogBackground.animate()
                .alpha(0f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator()),
            dialogContainer.animate()
                .alpha(0f)
                .scaleX(0.5f).scaleY(0.5f)
                .translationYBy(dialogContainer.height * 0.2f)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateInterpolator()),
            achievementIconView.animateTo(questBubbleView, root)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    dialogAndBackgroundContainer.visibility = View.INVISIBLE
                    questBubbleView.visibility = View.VISIBLE
                    this.achievementIconBubble = null
                }
        ))
        currentAnimators.forEach { it.start() }
    }

    private fun clearAnimators() {
        for (anim in currentAnimators) {
            anim.cancel()
        }
        currentAnimators.clear()
    }

    // TODO extension of fragment?
    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return tryStartActivity(intent)
    }

    private fun tryStartActivity(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    companion object {
        const val ANIMATION_TIME_IN_MS = 500L
        const val ANIMATION_TIME_OUT_MS = 300L
    }
}
