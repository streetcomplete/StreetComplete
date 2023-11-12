package de.westnordost.streetcomplete.quests.bbq_fuel

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.WOOD
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.ELECTRIC
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.CHARCOAL

class AddBbqFuelForm : AListQuestForm<BbqFuelAnswer>() {
    override val items = listOf(
        TextItem(WOOD, R.string.quest_bbq_fuel_wood),
        TextItem(ELECTRIC, R.string.quest_bbq_fuel_electric),
        TextItem(CHARCOAL, R.string.quest_bbq_fuel_charcoal),
    )

    override val otherAnswers = listOfNotNull(
        AnswerItem(R.string.quest_bbq_fuel_not_a_bbq) { confirmNotBbq() },
    )

    private fun confirmNotBbq() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_bbq_fuel_not_a_bbq_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NOT_BBQ) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
