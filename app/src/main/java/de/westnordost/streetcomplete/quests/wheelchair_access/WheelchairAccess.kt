package de.westnordost.streetcomplete.quests.wheelchair_access

sealed interface WheelChairAccessAnswer

enum class WheelchairAccess(val osmValue: String) : WheelChairAccessAnswer {
    YES("yes"),
    LIMITED("limited"),
    NO("no"),
}

object NoToilet : WheelChairAccessAnswer
