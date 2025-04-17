package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.ACTUALLY_HAS_SIDEWALK
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.NO
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.YES

class AddProhibitedForPedestriansForm : AListQuestForm<ProhibitedForPedestriansAnswer>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_accessible_for_pedestrians_prohibited),
        TextItem(NO, R.string.quest_accessible_for_pedestrians_allowed),
        TextItem(ACTUALLY_HAS_SIDEWALK, R.string.quest_accessible_for_pedestrians_sidewalk),
    )
}
