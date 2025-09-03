package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

sealed interface FireHydrantDiameterAnswer {
    data object NoSign : FireHydrantDiameterAnswer
}

data class FireHydrantDiameter(val value: Int, val unit: Unit) : FireHydrantDiameterAnswer {
    fun toOsmValue() = value.toString() + when (unit) {
        Unit.Millimeter -> ""
        Unit.Inch -> "\""
    }
    enum class Unit { Millimeter, Inch }
}
