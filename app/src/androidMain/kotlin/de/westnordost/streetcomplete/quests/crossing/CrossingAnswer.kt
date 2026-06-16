package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class CrossingAnswer {
    YES,
    INFORMAL,
    PROHIBITED
}

val CrossingAnswer.text: StringResource get() = when (this) {
    YES -> Res.string.quest_crossing_yes
    INFORMAL -> Res.string.quest_crossing_no
    PROHIBITED -> Res.string.quest_crossing_prohibited
}
