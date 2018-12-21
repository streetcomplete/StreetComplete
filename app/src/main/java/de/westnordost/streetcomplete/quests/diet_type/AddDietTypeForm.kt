package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no_only.*
import kotlinx.android.synthetic.main.quest_diet_type_explanation.*

class AddDietTypeForm : AbstractQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        setContentView(R.layout.quest_diet_type_explanation)
        setButtonsView(R.layout.quest_buttonpanel_yes_no_only)

        yesButton.setOnClickListener { onClickAnswer("yes") }
        noButton.setOnClickListener { onClickAnswer("no") }
        onlyButton.setOnClickListener { onClickAnswer("only") }

        val resId = arguments?.getInt(ARG_DIET) ?: 0
        if (resId > 0) {
            descriptionLabel.setText(resId)
        } else {
            descriptionLabel.visibility = View.GONE
        }

        return view
    }

    protected fun onClickAnswer(answer: String) {
        val bundle = Bundle()
        bundle.putString(OSM_VALUE, answer)
        applyAnswer(bundle)
    }

    companion object {
        val OSM_VALUE = "answer"

        private val ARG_DIET = "diet_explanation"

        fun create(dietExplanationResId: Int): AddDietTypeForm {
            val form = AddDietTypeForm()
            val args = Bundle()
            args.putInt(ARG_DIET, dietExplanationResId)
            form.arguments = args
            return form
        }
    }
}
