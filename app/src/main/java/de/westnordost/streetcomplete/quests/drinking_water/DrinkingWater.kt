package de.westnordost.streetcomplete.quests.drinking_water

enum class DrinkingWater(val osmValue: String, val osmLegalValue: String?) {
    DRINKABLE_SIGN("yes", "yes"),
    DRINKABLE("yes", null),
    NOT_DRINKABLE_SIGN("no", "no"),
    NOT_DRINKABLE("no", null),
}
