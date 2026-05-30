package de.westnordost.streetcomplete.quests.diet_type

sealed interface DietAvailabilityAnswer

enum class DietAvailability(val osmValue: String) : DietAvailabilityAnswer {
    YES("yes"),
    NO("no"),
    ONLY("only"),
}

data object NoFood : DietAvailabilityAnswer
