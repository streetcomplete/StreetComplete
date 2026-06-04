package de.westnordost.streetcomplete.quests.diet_type

sealed interface DietAvailabilityAnswer {
    data object NoFood : DietAvailabilityAnswer
}

enum class DietAvailability(val osmValue: String) : DietAvailabilityAnswer {
    YES("yes"),
    NO("no"),
    ONLY("only"),
}
