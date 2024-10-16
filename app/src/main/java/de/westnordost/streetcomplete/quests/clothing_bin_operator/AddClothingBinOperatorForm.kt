package de.westnordost.streetcomplete.quests.clothing_bin_operator

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddClothingBinOperatorForm : ANameWithSuggestionsForm<ClothingBinOperatorAnswer>() {

    override val suggestions: List<String>? get() = countryInfo.clothesContainerOperators

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_generic_answer_noSign) { confirmNoSign() }
    )

    override fun onClickOk() {
        applyAnswer(ClothingBinOperator(name!!))
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoClothingBinOperatorSigned) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
