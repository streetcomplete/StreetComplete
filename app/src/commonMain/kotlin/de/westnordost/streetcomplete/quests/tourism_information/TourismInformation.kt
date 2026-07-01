package de.westnordost.streetcomplete.quests.tourism_information

import kotlinx.serialization.Serializable

@Serializable
enum class TourismInformation(val osmValue: String) {
    OFFICE("office"),
    BOARD("board"),
    TERMINAL("terminal"),
    MAP("map"),
    GUIDEPOST("guidepost"),
}
