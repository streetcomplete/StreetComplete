package de.westnordost.streetcomplete.user

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.util.Transforms
import de.westnordost.streetcomplete.util.animateFrom
import de.westnordost.streetcomplete.util.animateTo
import de.westnordost.streetcomplete.util.applyTransforms
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import kotlinx.android.synthetic.main.fragment_quest_type_info.*
import kotlin.math.min
import kotlin.math.pow

/** Shows the details for a certain quest type as a fake-dialog.
 *
 *  It is not a real dialog because a real dialog has its own window, or in other words, has a
 *  different root view than the rest of the UI. However, for the calculation to animate the icon
 *  from another view to the position in the "dialog", there must be a common root view.*/
class QuestTypeInfoFragment : Fragment(R.layout.fragment_quest_type_info) {

    /** View from which the quest icon is animated from (and back on dismissal)*/
    private var questBubbleView: View? = null

    var isShowing: Boolean = false
        private set

    // need to keep the animators here to be able to clear them on cancel
    private val currentAnimators: MutableList<ViewPropertyAnimator> = mutableListOf()
    private var counterAnimation: ValueAnimator? = null

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            questIconView.outlineProvider = CircularOutlineProvider
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        questBubbleView = null
        clearAnimators()
        counterAnimation?.cancel()
        counterAnimation = null
    }


    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(questType: QuestType<*>, questCount: Int, questBubbleView: View) {
        isShowing = true
        this.questBubbleView = questBubbleView

        bind(questType, questCount)

        animateIn(questBubbleView)
    }

    fun dismiss() {
        isShowing = false
        questBubbleView?.let { animateOut(it) }
    }

    /* ----------------------------------- Animating in and out --------------------------------- */

    private fun bind(questType: QuestType<*>, questCount: Int) {
        questIconView.setImageResource(questType.icon)
        questTitleText.text = resources.getString(questType.title, *Array(10){"…"})
        solvedQuestsText.text = ""
        val scale = (0.4 + min( questCount / 100.0, 1.0)*0.6).toFloat()
        solvedQuestsContainer.visibility = View.INVISIBLE
        solvedQuestsContainer.scaleX = scale
        solvedQuestsContainer.scaleY = scale
        if (questType is OsmElementQuestType && questType.wikiLink != null) {
            wikiLinkButton.visibility = View.VISIBLE
            wikiLinkButton.setOnClickListener {
                openUrl("https://wiki.openstreetmap.org/wiki/${questType.wikiLink}")
            }
        } else {
            wikiLinkButton.visibility = View.INVISIBLE
        }

        counterAnimation?.cancel()
        val anim = ValueAnimator.ofInt(0, questCount)

        anim.doOnStart { solvedQuestsContainer.visibility = View.VISIBLE }
        anim.duration = (questCount * 150.0).pow(0.75).toLong()
        anim.addUpdateListener { solvedQuestsText?.text = it.animatedValue.toString() }
        anim.interpolator = DecelerateInterpolator()
        anim.startDelay = ANIMATION_TIME_IN_MS
        anim.start()
        counterAnimation = anim
    }

    private fun animateIn(questBubbleView: View) {
        dialogAndBackgroundContainer.visibility = View.VISIBLE

        clearAnimators()
        currentAnimators.addAll(
            createDialogPopInAnimations() + listOf(
                createQuestIconFlingInAnimation(questBubbleView),
                createFadeInBackgroundAnimation()
            )
        )
        currentAnimators.forEach { it.start() }
    }

    private fun animateOut(questBubbleView: View) {
        clearAnimators()
        currentAnimators.addAll(
            createDialogPopOutAnimations() + listOf(
                createQuestIconFlingOutAnimation(questBubbleView),
                createFadeOutBackgroundAnimation()
            )
        )
        currentAnimators.forEach { it.start() }
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

    private fun createQuestIconFlingInAnimation(sourceView: View): ViewPropertyAnimator {
        sourceView.visibility = View.INVISIBLE
        val root = sourceView.rootView as ViewGroup
        questIconView.applyTransforms(Transforms.IDENTITY)
        return questIconView.animateFrom(sourceView, root)
            .setDuration(ANIMATION_TIME_IN_MS)
            .setInterpolator(OvershootInterpolator())
    }

    private fun createQuestIconFlingOutAnimation(targetView: View): ViewPropertyAnimator {
        val root = targetView.rootView as ViewGroup
        return questIconView.animateTo(targetView, root)
            .setDuration(ANIMATION_TIME_OUT_MS)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                targetView.visibility = View.VISIBLE
                questBubbleView = null
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

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return tryStartActivity(intent)
    }

    companion object {
        const val ANIMATION_TIME_IN_MS = 600L
        const val ANIMATION_TIME_OUT_MS = 300L
    }
}
