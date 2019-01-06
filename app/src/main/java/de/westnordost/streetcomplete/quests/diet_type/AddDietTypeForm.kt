package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no_only.*
import kotlinx.android.synthetic.main.quest_diet_type_explanation.*

class AddDietTypeForm : AbstractQuestAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_diet_type_explanation
    override val buttonsResId = R.layout.quest_buttonpanel_yes_no_only

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { onClickAnswer("yes") }
        noButton.setOnClickListener { onClickAnswer("no") }
        onlyButton.setOnClickListener { onClickAnswer("only") }

        val resId = arguments?.getInt(ARG_DIET) ?: 0
        if (resId > 0) {
            descriptionLabel.setText(resId)
        } else {
            descriptionLabel.visibility = View.GONE
        }
    }

    protected fun onClickAnswer(answer: String) {
        val bundle = Bundle()
        bundle.putString(OSM_VALUE, answer)
        applyAnswer(bundle)
    }

    companion object {
        const val OSM_VALUE = "answer"

        private const val ARG_DIET = "diet_explanation"

        fun create(dietExplanationResId: Int): AddDietTypeForm {
            val form = AddDietTypeForm()
            val args = Bundle()
            args.putInt(ARG_DIET, dietExplanationResId)
            form.arguments = args
            return form
        }
    }
}
