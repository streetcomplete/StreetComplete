package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddSeatingForm : AListQuestForm<Seating>() {
    override val items = listOf(
        TextItem(Seating.INDOOR_AND_OUTDOOR, R.string.quest_seating_indoor_and_outdoor),
        TextItem(Seating.ONLY_INDOOR, R.string.quest_seating_indoor_only),
        TextItem(Seating.ONLY_OUTDOOR, R.string.quest_seating_outdoor_only),
        TextItem(Seating.NO, R.string.quest_seating_takeaway),
    )
}
