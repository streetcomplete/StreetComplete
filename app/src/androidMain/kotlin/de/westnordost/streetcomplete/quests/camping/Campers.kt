package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.quests.camping.Campers.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_camp_type_caravans_only
import de.westnordost.streetcomplete.resources.quest_camp_type_tents_and_caravans
import de.westnordost.streetcomplete.resources.quest_camp_type_tents_only
import org.jetbrains.compose.resources.StringResource

sealed interface CampTypeAnswer {
    data object IsBackcountry : CampTypeAnswer
}

enum class Campers(val tents: Boolean, val caravans: Boolean): CampTypeAnswer {
    TENTS_AND_CARAVANS(true, true),
    TENTS_ONLY(true, false),
    CARAVANS_ONLY(false, true),
}

val Campers.text: StringResource get() = when (this) {
    TENTS_AND_CARAVANS -> Res.string.quest_camp_type_tents_and_caravans
    TENTS_ONLY -> Res.string.quest_camp_type_tents_only
    CARAVANS_ONLY -> Res.string.quest_camp_type_caravans_only
}
