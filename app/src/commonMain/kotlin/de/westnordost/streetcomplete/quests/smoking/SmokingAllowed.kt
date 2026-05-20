package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class SmokingAllowed(val osmValue: String) {
    YES("yes"),
    OUTSIDE("outside"),
    NO("no"),
    SEPARATED("separated");

    companion object {
        fun getSelectableValues(tags: Map<String, String>): List<SmokingAllowed> {
            val isAlreadyOutdoor =
                tags["leisure"] == "outdoor_seating" ||
                tags["amenity"] == "biergarten" ||
                (tags["outdoor_seating"] == "yes" && tags["indoor_seating"] == "no")
            val noOutdoorSmoking =
                tags["outdoor_seating"] == "no" &&
                tags["amenity"] != "nightclub" &&
                tags["amenity"] != "stripclub" &&
                tags["amenity"] != "pub"
            // nightclubs etc. might have outside smoking areas even when there is no seating outside

            return listOfNotNull(
                NO,
                if (isAlreadyOutdoor || noOutdoorSmoking) null else OUTSIDE,
                SEPARATED,
                YES,
            )
        }
    }
}

val SmokingAllowed.text: StringResource get() = when (this) {
    YES -> Res.string.quest_smoking_yes
    OUTSIDE -> Res.string.quest_smoking_outside
    NO -> Res.string.quest_smoking_no
    SEPARATED -> Res.string.quest_smoking_separated
}
