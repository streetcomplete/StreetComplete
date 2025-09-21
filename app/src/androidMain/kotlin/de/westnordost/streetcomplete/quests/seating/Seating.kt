package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.quests.seating.Seating.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_seating_indoor_and_outdoor
import de.westnordost.streetcomplete.resources.quest_seating_indoor_only
import de.westnordost.streetcomplete.resources.quest_seating_outdoor_only
import de.westnordost.streetcomplete.resources.quest_seating_takeaway
import org.jetbrains.compose.resources.StringResource

enum class Seating(val hasOutdoorSeating: Boolean, val hasIndoorSeating: Boolean) {
    INDOOR_AND_OUTDOOR(true, true),
    ONLY_INDOOR(false, true),
    ONLY_OUTDOOR(true, false),
    TAKEAWAY_ONLY(false, false),
}

val Seating.text: StringResource get() = when (this) {
    INDOOR_AND_OUTDOOR -> Res.string.quest_seating_indoor_and_outdoor
    ONLY_INDOOR -> Res.string.quest_seating_indoor_only
    ONLY_OUTDOOR -> Res.string.quest_seating_outdoor_only
    TAKEAWAY_ONLY -> Res.string.quest_seating_takeaway
}
