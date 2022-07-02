package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.NOT_POTABLE_SIGNED
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.NOT_POTABLE_UNSIGNED
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.POTABLE_SIGNED
import de.westnordost.streetcomplete.quests.drinking_water.DrinkingWater.POTABLE_UNSIGNED

class AddDrinkingWaterForm : AListQuestForm<DrinkingWater>() {

    override val items = listOf(
        TextItem(POTABLE_SIGNED, R.string.quest_drinking_water_potable_signed),
        TextItem(POTABLE_UNSIGNED, R.string.quest_drinking_water_potable_unsigned),
        TextItem(NOT_POTABLE_SIGNED, R.string.quest_drinking_water_not_potable_signed),
        TextItem(NOT_POTABLE_UNSIGNED, R.string.quest_drinking_water_not_potable_unsigned),
    )
}
