package de.westnordost.streetcomplete.quests.board_type

import de.westnordost.streetcomplete.quests.board_type.BoardType.*
import de.westnordost.streetcomplete.resources.*

import org.jetbrains.compose.resources.StringResource

sealed interface BoardTypeAnswer {
    data object NoBoardJustMap : BoardTypeAnswer
    data class BoardTypes(val boardTypes: Set<BoardType>) : BoardTypeAnswer
}

enum class BoardType(val osmValue: String) {
    HISTORY("history"),
    GEOLOGY("geology"),
    PLANTS("plants"),
    WILDLIFE("wildlife"),
    NATURE("nature"),
    PUBLIC_TRANSPORT("public_transport"),
    NOTICE("notice"),
    SPORT("sport"),
    RULES("rules")
}

val BoardType.text: StringResource get() = when (this) {
    HISTORY -> Res.string.quest_board_type_history
    GEOLOGY -> Res.string.quest_board_type_geology
    PLANTS -> Res.string.quest_board_type_plants
    WILDLIFE -> Res.string.quest_board_type_wildlife
    NATURE -> Res.string.quest_board_type_nature
    PUBLIC_TRANSPORT -> Res.string.quest_board_type_public_transport
    NOTICE -> Res.string.quest_board_type_notice_board
    SPORT -> Res.string.quest_board_type_sport
    RULES -> Res.string.quest_board_type_rules
}
