package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.Toast

import de.westnordost.streetcomplete.R

/** Abstract base class for dialogs in which the user answers a quest with a form he has to fill
 * out  */
abstract class AbstractQuestFormAnswerFragment : AbstractQuestAnswerFragment() {
    private lateinit var okButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        okButton = view!!.findViewById(R.id.okButton)
        okButton.setOnClickListener {
            if (!isFormComplete()) {
                Toast.makeText(activity, R.string.no_changes, Toast.LENGTH_SHORT).show()
            } else {
                onClickOk()
            }
        }
        return view
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
                .withEndAction { okButton.visibility = View.GONE }
        }
    }

    protected abstract fun onClickOk()

	abstract fun isFormComplete(): Boolean

	override fun isRejectingClose() = isFormComplete()
}
