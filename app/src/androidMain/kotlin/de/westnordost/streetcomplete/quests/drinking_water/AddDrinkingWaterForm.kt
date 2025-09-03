package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_drinking_water_not_potable_signed
import de.westnordost.streetcomplete.resources.quest_drinking_water_potable_signed
import de.westnordost.streetcomplete.resources.quest_drinking_water_potable_unsigned2
import de.westnordost.streetcomplete.ui.common.TextItem

class AddDrinkingWaterForm : AListQuestForm<DrinkingWater>() {

    override val items = listOf(
        TextItem(DrinkingWater.POTABLE_SIGNED, Res.string.quest_drinking_water_potable_signed),
        TextItem(DrinkingWater.NOT_POTABLE_SIGNED, Res.string.quest_drinking_water_not_potable_signed),
        TextItem(DrinkingWater.UNSIGNED, Res.string.quest_drinking_water_potable_unsigned2),
    )
}
