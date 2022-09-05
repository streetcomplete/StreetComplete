package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddPowerPolesMaterialForm : AImageListQuestForm<PowerPolesMaterial, PowerPolesMaterial>() {

    override val items = PowerPolesMaterial.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PowerPolesMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
