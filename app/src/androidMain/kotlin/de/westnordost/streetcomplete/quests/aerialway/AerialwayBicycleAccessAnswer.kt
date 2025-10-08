package de.westnordost.streetcomplete.quests.aerialway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_no
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_yes
import de.westnordost.streetcomplete.resources.quest_hairdresser_not_signed
import org.jetbrains.compose.resources.StringResource

enum class AerialwayBicycleAccessAnswer {
    YES,
    SUMMER,
    NO_SIGN,
    NO
}

val AerialwayBicycleAccessAnswer.text: StringResource get() = when (this) {
    YES -> Res.string.quest_generic_hasFeature_yes
    SUMMER -> Res.string.quest_aerialway_bicycle_summer
    NO_SIGN -> Res.string.quest_hairdresser_not_signed
    NO -> Res.string.quest_generic_hasFeature_no
}
