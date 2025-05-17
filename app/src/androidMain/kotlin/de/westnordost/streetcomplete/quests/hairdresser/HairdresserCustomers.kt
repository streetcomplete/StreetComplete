package de.westnordost.streetcomplete.quests.hairdresser

enum class HairdresserCustomers(val isMale: Boolean, val isFemale: Boolean) {
    NOT_SIGNED(false, false),
    ONLY_FEMALE(false, true),
    ONLY_MALE(true, false),
    MALE_AND_FEMALE(true, true),
}
