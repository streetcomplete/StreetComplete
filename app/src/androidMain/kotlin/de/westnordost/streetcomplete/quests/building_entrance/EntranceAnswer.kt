package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.quests.building_entrance.EntranceType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_building_entrance_dead_end
import de.westnordost.streetcomplete.resources.quest_building_entrance_emergency_exit
import de.westnordost.streetcomplete.resources.quest_building_entrance_exit
import de.westnordost.streetcomplete.resources.quest_building_entrance_main
import de.westnordost.streetcomplete.resources.quest_building_entrance_service
import de.westnordost.streetcomplete.resources.quest_building_entrance_shop
import de.westnordost.streetcomplete.resources.quest_building_entrance_staircase
import de.westnordost.streetcomplete.resources.quest_building_entrance_yes
import org.jetbrains.compose.resources.StringResource

sealed interface EntranceAnswer {
    data object IsDeadEnd : EntranceAnswer
}


enum class EntranceType(val osmValue: String) : EntranceAnswer {
    MAIN("main"),
    STAIRCASE("staircase"),
    SERVICE("service"),
    EMERGENCY_EXIT("emergency"),
    EXIT("exit"),
    SHOP("shop"),
    GENERIC("yes"),
}

val EntranceType.text: StringResource get() = when (this) {
    MAIN -> Res.string.quest_building_entrance_main
    STAIRCASE -> Res.string.quest_building_entrance_staircase
    SERVICE -> Res.string.quest_building_entrance_service
    EMERGENCY_EXIT -> Res.string.quest_building_entrance_emergency_exit
    EXIT -> Res.string.quest_building_entrance_exit
    SHOP -> Res.string.quest_building_entrance_shop
    GENERIC -> Res.string.quest_building_entrance_yes
}

val EntranceAnswer.text: StringResource get() = when (this) {
    EntranceAnswer.IsDeadEnd -> Res.string.quest_building_entrance_dead_end
    is EntranceType -> text
}
