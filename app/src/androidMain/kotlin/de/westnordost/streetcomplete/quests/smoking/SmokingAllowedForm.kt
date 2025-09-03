package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.NO
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.OUTSIDE
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.SEPARATED
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_smoking_no
import de.westnordost.streetcomplete.resources.quest_smoking_outside
import de.westnordost.streetcomplete.resources.quest_smoking_separated
import de.westnordost.streetcomplete.resources.quest_smoking_yes
import de.westnordost.streetcomplete.ui.common.TextItem

class SmokingAllowedForm : AListQuestForm<SmokingAllowed>() {

    override val items: List<TextItem<SmokingAllowed>> get() {
        val tags = element.tags
        val isAlreadyOutdoor =
            tags["leisure"] == "outdoor_seating" || tags["amenity"] == "biergarten" ||
            (tags["outdoor_seating"] == "yes" && tags["indoor_seating"] == "no")
        val noOutdoorSmoking =
            tags["outdoor_seating"] == "no" &&
            tags["amenity"] != "nightclub" && tags["amenity"] != "stripclub" && tags["amenity"] != "pub"
            // nightclubs etc. might have outside smoking areas even when there is no seating outside

        return listOfNotNull(
            TextItem(NO, Res.string.quest_smoking_no),
            if (isAlreadyOutdoor || noOutdoorSmoking) null else TextItem(OUTSIDE, Res.string.quest_smoking_outside),
            TextItem(SEPARATED, Res.string.quest_smoking_separated),
            TextItem(YES, Res.string.quest_smoking_yes),
        )
    }
}
