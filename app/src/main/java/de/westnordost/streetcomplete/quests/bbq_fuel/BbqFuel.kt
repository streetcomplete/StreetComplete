package de.westnordost.streetcomplete.quests.bbq_fuel

sealed interface BbqFuelAnswer

enum class BbqFuel(val osmValue: String) : BbqFuelAnswer {
    WOOD("wood"),
    ELECTRIC("electric"),
    CHARCOAL("charcoal")
}

object NotBbq : BbqFuelAnswer
