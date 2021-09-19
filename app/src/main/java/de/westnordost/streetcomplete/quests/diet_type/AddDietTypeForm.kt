package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestDietTypeExplanationBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.*

class AddDietTypeForm : AbstractQuestAnswerFragment<DietAvailability>() {

    override val contentLayoutResId = R.layout.quest_diet_type_explanation
    private val binding by contentViewBinding(QuestDietTypeExplanationBinding::bind)

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(DIET_NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(DIET_YES) },
        AnswerItem(R.string.quest_hasFeature_only) { applyAnswer(DIET_ONLY) },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resId = arguments?.getInt(ARG_DIET) ?: 0
        if (resId > 0) {
            binding.descriptionLabel.setText(resId)
        } else {
            binding.descriptionLabel.visibility = View.GONE
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
