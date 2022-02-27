package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.YES
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.OUTSIDE
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.NO
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.SEPARATED

class SmokingAllowedAnswerForm : AListQuestAnswerFragment<SmokingAllowed>() {

    override val items: List<TextItem<SmokingAllowed>> get() {
        val tags = osmElement!!.tags
        val isAlwaysOutsideOnly =
            tags["leisure"] == "outdoor_seating" || tags["amenity"] == "biergarten" ||
            (tags["outdoor_seating"] == "yes" && tags["indoor_seating"] == "no")

        val result = if (isAlwaysOutsideOnly) listOf(
            TextItem(NO, R.string.quest_smoking_no),
            TextItem(SEPARATED, R.string.quest_smoking_separated),
            TextItem(YES, R.string.quest_smoking_yes),
        )
        else listOf(
            TextItem(NO, R.string.quest_smoking_no),
            TextItem(OUTSIDE, R.string.quest_smoking_outside),
            TextItem(SEPARATED, R.string.quest_smoking_separated),
            TextItem(YES, R.string.quest_smoking_yes),
        )
        return result
    }
}
