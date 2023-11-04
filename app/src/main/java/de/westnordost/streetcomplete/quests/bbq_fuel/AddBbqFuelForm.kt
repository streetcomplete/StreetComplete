package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.WOOD
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.ELECTRIC
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.CHARCOAL
import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.NOT_BBQ

class AddBbqFuelForm : AListQuestForm<BbqFuelAnswer>() {
    override val items = listOf(
        TextItem(WOOD, R.string.quest_bbq_fuel_wood),
        TextItem(ELECTRIC, R.string.quest_bbq_fuel_electric),
        TextItem(CHARCOAL, R.string.quest_bbq_fuel_charcoal),
    )

    override val otherAnswers = listOfNotNull(
        AnswerItem(R.string.quest_bbq_fuel_not_a_bbq) { applyAnswer(NOT_BBQ) },
    )
}
