package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestDietTypeExplanationBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_NO
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_ONLY
import de.westnordost.streetcomplete.quests.diet_type.DietAvailability.DIET_YES

class AddDietTypeForm : AbstractOsmQuestForm<DietAvailabilityAnswer>() {

    override val otherAnswers: List<AnswerItem> get() {
        val result = mutableListOf<AnswerItem>()
        if (element.tags["amenity"] == "cafe") {
            result.add(AnswerItem(R.string.quest_diet_answer_no_food) { confirmNoFood() })
        }
        return result
    }

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

    private fun confirmNoFood() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFood) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
