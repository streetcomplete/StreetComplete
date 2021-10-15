package de.westnordost.streetcomplete.quests.fire_hydrant

sealed interface FireHydrantDiameterAnswer

object NoFireHydrantDiameterSign : FireHydrantDiameterAnswer
data class FireHydrantDiameter(val diameter: Int) : FireHydrantDiameterAnswer
