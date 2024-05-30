package de.westnordost.streetcomplete.quests.powerpoles_material

sealed interface PowerPolesMaterialAnswer
enum class PowerPolesMaterial(val osmValue: String) : PowerPolesMaterialAnswer {
    WOOD("wood"),
    STEEL("steel"),
    CONCRETE("concrete")
}

data object PowerLineAnchoredToBuilding : PowerPolesMaterialAnswer
