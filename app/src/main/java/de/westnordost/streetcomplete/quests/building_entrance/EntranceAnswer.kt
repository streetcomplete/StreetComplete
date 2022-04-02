package de.westnordost.streetcomplete.quests.building_entrance

sealed interface EntranceAnswer

object DeadEnd : EntranceAnswer

enum class EntranceExistsAnswer(val osmValue: String) : EntranceAnswer {
    MAIN("main"),
    SECONDARY("secondary"),
    SERVICE("service"),
    GARAGE("garage"),
    EMERGENCY_EXIT("emergency"),
    EXIT("exit"),
    SHOP("shop"),
    GENERIC("yes"),
}
