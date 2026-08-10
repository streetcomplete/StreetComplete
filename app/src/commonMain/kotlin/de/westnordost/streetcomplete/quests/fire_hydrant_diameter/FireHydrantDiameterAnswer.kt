package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.Inch
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameter.Unit.Millimeter
import kotlinx.serialization.Serializable

sealed interface FireHydrantDiameterAnswer {
    data object NoSign : FireHydrantDiameterAnswer
}

@Serializable
data class FireHydrantDiameter(val value: Int, val unit: Unit) : FireHydrantDiameterAnswer {
    fun toOsmValue(): String =
        value.toString() + when (unit) {
            Unit.Millimeter -> ""
            Unit.Inch -> "\""
        }

    @Serializable
    enum class Unit {
        Millimeter, Inch;

        fun usualRange(): IntProgression = when (this) {
            Millimeter -> 50..600 step 5
            Inch -> 1..25
        }
    }

    fun isUnusual(): Boolean =
        value !in unit.usualRange()
}
