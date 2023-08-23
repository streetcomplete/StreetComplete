package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddBBQFuelForm : AListQuestForm<BBQFuel>() {

    override val items = listOf(
        TextItem(BBQFuel.WOOD, R.string.quest_bbq_fuel_wood),
        TextItem(BBQFuel.ELECTRIC, R.string.quest_bbq_fuel_electric),
        TextItem(BBQFuel.CHARCOAL, R.string.quest_bbq_fuel_charcoal),
    )

}
