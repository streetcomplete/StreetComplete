package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.CONCRETE
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.STEEL
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.WOOD
import de.westnordost.streetcomplete.view.image_select.Item

class AddPowerPolesMaterialForm : AImageListQuestForm<PowerPolesMaterial, PowerPolesMaterial>() {

    override val items = listOf(
        Item(WOOD, R.drawable.power_pole_wood, R.string.quest_powerPolesMaterial_wood),
        Item(STEEL, R.drawable.power_pole_steel, R.string.quest_powerPolesMaterial_metal),
        Item(CONCRETE, R.drawable.power_pole_concrete, R.string.quest_powerPolesMaterial_concrete)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PowerPolesMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
