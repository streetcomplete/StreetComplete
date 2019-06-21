package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddPowerPolesMaterialForm : AImageListQuestAnswerFragment<String,String>() {

    override val items = listOf(
        Item("wood", R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
        Item("steel", R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
        Item("concrete", R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
