package de.westnordost.streetcomplete.quests.swimming_pool_availability

enum class SwimmingPoolAvailability(val osmValue: String) {
    NO("no"),
    ONLY_INDOOR("indoor"),
    ONLY_OUTDOOR("outdoor"),
    INDOOR_AND_OUTDOOR("indoor;outdoor"),
}
