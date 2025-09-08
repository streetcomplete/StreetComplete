package de.westnordost.streetcomplete.quests.diet_type

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
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

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(DIET_NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(DIET_YES) },
        AnswerItem(R.string.quest_hasFeature_only) { applyAnswer(DIET_ONLY) },
    )

    private fun confirmNoFood() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoFood) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
