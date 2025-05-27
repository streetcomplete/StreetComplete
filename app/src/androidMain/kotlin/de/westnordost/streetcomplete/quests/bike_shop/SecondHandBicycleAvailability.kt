package de.westnordost.streetcomplete.quests.bike_shop

enum class SecondHandBicycleAvailability(val osmValue: String?) {
    ONLY_NEW("no"),
    NEW_AND_SECOND_HAND("yes"),
    ONLY_SECOND_HAND("only"),
    NO_BICYCLES_SOLD(null),
}
