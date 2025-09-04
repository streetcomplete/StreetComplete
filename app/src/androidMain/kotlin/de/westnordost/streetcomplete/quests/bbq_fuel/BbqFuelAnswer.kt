package de.westnordost.streetcomplete.quests.bbq_fuel

import de.westnordost.streetcomplete.quests.bbq_fuel.BbqFuel.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bbq_fuel_charcoal
import de.westnordost.streetcomplete.resources.quest_bbq_fuel_electric
import de.westnordost.streetcomplete.resources.quest_bbq_fuel_gas
import de.westnordost.streetcomplete.resources.quest_bbq_fuel_wood
import org.jetbrains.compose.resources.StringResource

sealed interface BbqFuelAnswer {
    data object IsFirePit : BbqFuelAnswer
}

enum class BbqFuel(val osmValue: String) : BbqFuelAnswer {
    WOOD("wood"),
    ELECTRIC("electric"),
    CHARCOAL("charcoal"),
    GAS("gas"),
}

val BbqFuel.text: StringResource get() = when (this) {
    WOOD -> Res.string.quest_bbq_fuel_wood
    ELECTRIC -> Res.string.quest_bbq_fuel_electric
    CHARCOAL -> Res.string.quest_bbq_fuel_charcoal
    GAS -> Res.string.quest_bbq_fuel_gas
}
