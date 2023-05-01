package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*
import de.westnordost.streetcomplete.quests.TextItem

class AddCrossingForm : AListQuestForm<CrossingAnswer>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_crossing_yes),
        TextItem(NO, R.string.quest_crossing_no),
        TextItem(PROHIBITED, R.string.quest_crossing_prohibited),
    )
}
