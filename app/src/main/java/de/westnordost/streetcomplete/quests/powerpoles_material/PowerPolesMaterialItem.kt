package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.CONCRETE
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.STEEL
import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.WOOD
import de.westnordost.streetcomplete.view.image_select.Item

fun PowerPolesMaterial.asItem() = Item(this, iconResId, titleResId)

private val PowerPolesMaterial.titleResId: Int get() = when (this) {
    WOOD ->     R.string.quest_powerPolesMaterial_wood
    STEEL ->    R.string.quest_powerPolesMaterial_metal
    CONCRETE -> R.string.quest_powerPolesMaterial_concrete
}

private val PowerPolesMaterial.iconResId: Int get() = when (this) {
    WOOD ->     R.drawable.power_pole_wood
    STEEL ->    R.drawable.power_pole_steel
    CONCRETE -> R.drawable.power_pole_concrete
}
