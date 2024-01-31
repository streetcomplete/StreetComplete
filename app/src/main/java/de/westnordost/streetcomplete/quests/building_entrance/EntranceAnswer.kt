package de.westnordost.streetcomplete.quests.building_entrance

sealed interface EntranceAnswer

data object DeadEnd : EntranceAnswer

enum class EntranceExistsAnswer(val osmValue: String) : EntranceAnswer {
    MAIN("main"),
    STAIRCASE("staircase"),
    SERVICE("service"),
    EMERGENCY_EXIT("emergency"),
    EXIT("exit"),
    SHOP("shop"),
    GENERIC("yes"),
}
