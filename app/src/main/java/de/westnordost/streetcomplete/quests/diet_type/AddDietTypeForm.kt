package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no_only.*
import kotlinx.android.synthetic.main.quest_diet_type_explanation.*

class AddDietTypeForm : AbstractQuestAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_diet_type_explanation
    override val buttonsResId = R.layout.quest_buttonpanel_yes_no_only

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { applyAnswer("yes") }
        noButton.setOnClickListener { applyAnswer("no") }
        onlyButton.setOnClickListener { applyAnswer("only") }

        val resId = arguments?.getInt(ARG_DIET) ?: 0
        if (resId > 0) {
            descriptionLabel.setText(resId)
        } else {
            descriptionLabel.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_DIET = "diet_explanation"

        fun create(dietExplanationResId: Int): AddDietTypeForm {
            val form = AddDietTypeForm()
            form.arguments = bundleOf(ARG_DIET to dietExplanationResId)
            return form
        }
    }
}
