package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.android.synthetic.main.fragment_quest_answer.*

/** Abstract base class for dialogs in which the user answers a quest with a form he has to fill
 * out  */
abstract class AbstractQuestFormAnswerFragment<T> : AbstractQuestAnswerFragment<T>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        okButton.setOnClickListener {
            if (!isFormComplete()) {
                activity?.toast(R.string.no_changes)
            } else {
                onClickOk()
            }
        }
    }

    protected fun checkIsFormComplete() {
        if (isFormComplete()) {
            okButton.visibility = View.VISIBLE
            okButton.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction(null)
        } else {
            okButton.animate()
                .alpha(0f).scaleX(0.5f).scaleY(0.5f)
                .setDuration(100)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { okButton?.visibility = View.GONE }
        }
    }

    protected abstract fun onClickOk()

    abstract fun isFormComplete(): Boolean

    override fun isRejectingClose() = isFormComplete()
}
