package de.westnordost.streetcomplete.quests.drinking_water

enum class DrinkingWater(val osmValue: String, val osmLegalValue: String?) {
    POTABLE_SIGNED("yes", "yes"),
    POTABLE_UNSIGNED("yes", null),
    NOT_POTABLE_SIGNED("no", "no"),
    NOT_POTABLE_UNSIGNED("no", null),
}
