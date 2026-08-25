package de.westnordost.streetcomplete.quests.powerpoles_material

import kotlinx.serialization.Serializable

sealed interface PowerPolesMaterialAnswer

@Serializable
enum class PowerPolesMaterial(val osmValue: String) : PowerPolesMaterialAnswer {
    WOOD("wood"),
    STEEL("steel"),
    CONCRETE("concrete")
}

data object PowerLineAnchoredToBuilding : PowerPolesMaterialAnswer
