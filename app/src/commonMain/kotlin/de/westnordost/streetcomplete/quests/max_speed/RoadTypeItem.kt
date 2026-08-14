package de.westnordost.streetcomplete.quests.max_speed

import org.jetbrains.compose.resources.DrawableResource
import de.westnordost.streetcomplete.quests.max_speed.RoadType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

val RoadType.icon: DrawableResource get() = when (this) {
    RURAL -> Res.drawable.maxspeed_roadtype_rural
    URBAN -> Res.drawable.maxspeed_roadtype_urban

    RESTRICTED -> Res.drawable.maxspeed_roadtype_lit
    SINGLE -> Res.drawable.maxspeed_roadtype_lit_no
    DUAL -> Res.drawable.maxspeed_roadtype_dual_carriageway
}

val RoadType.text: StringResource get() = when (this) {
    RURAL -> Res.string.quest_maxspeed_answer_roadtype_rural
    URBAN -> Res.string.quest_maxspeed_answer_roadtype_urban

    RESTRICTED -> Res.string.quest_maxspeed_answer_roadtype_lit
    SINGLE -> Res.string.quest_maxspeed_answer_roadtype_not_lit
    DUAL -> Res.string.quest_maxspeed_answer_roadtype_dual_carriageway
}
