package de.westnordost.streetcomplete.quests.bbq_fuel

sealed interface BbqFuelAnswer

enum class BbqFuel(val osmValue: String) {
    WOOD("wood"),
    ELECTRIC("electric"),
    CHARCOAL("charcoal")
} : BbqFuelAnswer

object NotBbq : BbqFuelAnswer
