package de.westnordost.streetcomplete.quests.fire_hydrant_diameter
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.INCH
import de.westnordost.streetcomplete.quests.fire_hydrant_diameter.FireHydrantDiameterMeasurementUnit.MILLIMETER

sealed interface FireHydrantDiameterAnswer

data object NoFireHydrantDiameterSign : FireHydrantDiameterAnswer
data class FireHydrantDiameter(val value: Int, val unit: FireHydrantDiameterMeasurementUnit) : FireHydrantDiameterAnswer {
    fun toOsmValue() = value.toString() + when (unit) {
        MILLIMETER -> ""
        INCH -> "\""
    }
}

enum class FireHydrantDiameterMeasurementUnit { MILLIMETER, INCH }
