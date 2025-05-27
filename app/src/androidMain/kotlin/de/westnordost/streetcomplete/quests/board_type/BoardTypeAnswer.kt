package de.westnordost.streetcomplete.quests.board_type

sealed interface BoardTypeAnswer
enum class BoardType(val osmValue: String) : BoardTypeAnswer {
    HISTORY("history"),
    GEOLOGY("geology"),
    PLANTS("plants"),
    WILDLIFE("wildlife"),
    NATURE("nature"),
    PUBLIC_TRANSPORT("public_transport"),
    NOTICE("notice"),
    SPORT("sport")
}
data object NoBoardJustMap : BoardTypeAnswer
