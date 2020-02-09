package de.westnordost.streetcomplete.user

import android.animation.ValueAnimator
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
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.util.Transforms
import de.westnordost.streetcomplete.util.animateFrom
import de.westnordost.streetcomplete.util.animateTo
import de.westnordost.streetcomplete.util.applyTransforms
import kotlinx.android.synthetic.main.fragment_quest_type_info.*
import kotlin.math.min
import kotlin.math.pow

class QuestTypeInfoFragment : Fragment(R.layout.fragment_quest_type_info) {

    private var questBubbleView: View? = null

    var isShowing: Boolean = false
        private set

    private val currentAnimators: MutableList<ViewPropertyAnimator> = mutableListOf()
    private var counterAnimation: ValueAnimator? = null

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogAndBackgroundContainer.setOnClickListener { dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        questBubbleView = null
        clearAnimators()
        counterAnimation?.cancel()
        counterAnimation = null
    }


    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(questBubbleView: View, questType: QuestType<*>, questCount: Int) {
        isShowing = true
        this.questBubbleView = questBubbleView

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

        animateIn(questBubbleView)

        val anim = ValueAnimator.ofInt(0, questCount)

        anim.doOnStart { solvedQuestsContainer.visibility = View.VISIBLE }
        anim.duration = (questCount * 150.0).pow(0.75).toLong()
        anim.addUpdateListener { solvedQuestsText?.text = it.animatedValue.toString() }
        anim.interpolator = DecelerateInterpolator()
        anim.startDelay = ANIMATION_TIME_IN_MS
        anim.start()
        counterAnimation = anim
    }

    fun dismiss() {
        isShowing = false
        questBubbleView?.let { animateOut(it) }
    }

    private fun animateIn(questBubbleView: View) {
        questBubbleView.visibility = View.INVISIBLE
        val root = questBubbleView.rootView as ViewGroup

        dialogAndBackgroundContainer.visibility = View.VISIBLE

        questIconView.applyTransforms(Transforms.IDENTITY)

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
            questIconView.animateFrom(questBubbleView, root)
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
            questIconView.animateTo(questBubbleView, root)
                .setDuration(ANIMATION_TIME_OUT_MS)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    dialogAndBackgroundContainer.visibility = View.INVISIBLE
                    questBubbleView.visibility = View.VISIBLE
                    this.questBubbleView = null
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
