package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.seating.Seating.INDOOR_AND_OUTDOOR
import de.westnordost.streetcomplete.quests.seating.Seating.NO
import de.westnordost.streetcomplete.quests.seating.Seating.ONLY_INDOOR
import de.westnordost.streetcomplete.quests.seating.Seating.ONLY_OUTDOOR
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_seating_indoor_and_outdoor
import de.westnordost.streetcomplete.resources.quest_seating_indoor_only
import de.westnordost.streetcomplete.resources.quest_seating_outdoor_only
import de.westnordost.streetcomplete.resources.quest_seating_takeaway
import de.westnordost.streetcomplete.ui.common.TextItem

class AddSeatingForm : AListQuestForm<Seating>() {
    override val items = listOf(
        TextItem(INDOOR_AND_OUTDOOR, Res.string.quest_seating_indoor_and_outdoor),
        TextItem(ONLY_INDOOR, Res.string.quest_seating_indoor_only),
        TextItem(ONLY_OUTDOOR, Res.string.quest_seating_outdoor_only),
        TextItem(NO, Res.string.quest_seating_takeaway),
    )
}
