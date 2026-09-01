package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

sealed interface BbqFuelAnswer {
    data object IsFirePit : BbqFuelAnswer
    data class Fuels(val fuels: Set<BbqFuel>) : BbqFuelAnswer
}

enum class BbqFuel(val bbqValue: String, val ovenValue: String) {
    WOOD("wood", "wood_fired"),
    ELECTRIC("electric", "electric"),
    CHARCOAL("charcoal", "charcoal"),
    GAS("gas", "gas_fired"),
}

val BbqFuel.text: StringResource get() = when (this) {
    WOOD -> Res.string.quest_bbq_fuel_wood
    ELECTRIC -> Res.string.quest_bbq_fuel_electric
    CHARCOAL -> Res.string.quest_bbq_fuel_charcoal
    GAS -> Res.string.quest_bbq_fuel_gas
}
