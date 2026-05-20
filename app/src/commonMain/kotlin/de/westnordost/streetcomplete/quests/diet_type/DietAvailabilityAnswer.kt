package de.westnordost.streetcomplete.quests.diet_type

sealed interface DietAvailabilityAnswer

enum class DietAvailability(val osmValue: String) : DietAvailabilityAnswer {
    DIET_YES("yes"),
    DIET_NO("no"),
    DIET_ONLY("only"),
}

data object NoFood : DietAvailabilityAnswer
