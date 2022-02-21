package de.westnordost.streetcomplete.quests.board_type

enum class BoardType(val osmValue: String) {
    HISTORY("history"),
    GEOLOGY("geology"),
    PLANTS("plants"),
    WILDLIFE("wildlife"),
    NATURE("nature"),
    PUBLIC_TRANSPORT("public_transport"),
    NOTICE("notice"),
    MAP("map"),
    SPORT("sport")
}
