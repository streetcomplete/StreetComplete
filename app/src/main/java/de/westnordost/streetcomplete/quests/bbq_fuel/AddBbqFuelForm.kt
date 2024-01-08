package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.CHARCOAL
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.ELECTRIC
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.WOOD

class AddBbqFuelForm : AListQuestForm<BbqFuelAnswer>() {
    override val items: List<TextItem<BbqFuelAnswer>> = listOf(
        TextItem(WOOD, R.string.quest_bbq_fuel_wood),
        TextItem(ELECTRIC, R.string.quest_bbq_fuel_electric),
        TextItem(CHARCOAL, R.string.quest_bbq_fuel_charcoal),
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bbq_fuel_not_a_bbq) { confirmNotBbq() },
    )

    private fun confirmNotBbq() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_bbq_fuel_not_a_bbq_confirmation)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsFirePitAnswer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
