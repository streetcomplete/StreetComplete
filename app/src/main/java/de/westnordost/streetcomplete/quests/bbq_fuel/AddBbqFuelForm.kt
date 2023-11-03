package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddBbqFuelForm : AListQuestForm<BbqFuelAnswer>() {

    override val items = listOf(
        TextItem(BbqFuel.WOOD, R.string.quest_bbq_fuel_wood),
        TextItem(BbqFuel.ELECTRIC, R.string.quest_bbq_fuel_electric),
        TextItem(BbqFuel.CHARCOAL, R.string.quest_bbq_fuel_charcoal),
    )

    override val otherAnswers = listOfNotNull(
        AnswerItem(R.string.quest_bbq_fuel_not_a_bbq) { applyAnswer(NotBbq) },

    )
}
