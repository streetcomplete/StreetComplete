package de.westnordost.streetcomplete.quests.powerpoles_material

import de.westnordost.streetcomplete.quests.powerpoles_material.PowerPolesMaterial.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.power_pole_concrete
import de.westnordost.streetcomplete.resources.power_pole_steel
import de.westnordost.streetcomplete.resources.power_pole_wood
import de.westnordost.streetcomplete.resources.quest_powerPolesMaterial_concrete
import de.westnordost.streetcomplete.resources.quest_powerPolesMaterial_metal
import de.westnordost.streetcomplete.resources.quest_powerPolesMaterial_wood
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PowerPolesMaterial.title: StringResource? get() = when (this) {
    WOOD ->     Res.string.quest_powerPolesMaterial_wood
    STEEL ->    Res.string.quest_powerPolesMaterial_metal
    CONCRETE -> Res.string.quest_powerPolesMaterial_concrete
}

val PowerPolesMaterial.icon: DrawableResource? get() = when (this) {
    WOOD ->     Res.drawable.power_pole_wood
    STEEL ->    Res.drawable.power_pole_steel
    CONCRETE -> Res.drawable.power_pole_concrete
}
