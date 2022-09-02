package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.CONCRETE
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.STEEL
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.WOOD
import de.westnordost.streetcomplete.view.image_select.Item

class AddPowerPolesMaterialForm : AImageListQuestForm<PowerPolesMaterial, PowerPolesMaterial>() {

    override val items = PowerPolesMaterial.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PowerPolesMaterial>) {
        applyAnswer(selectedItems.single())
    }
}
