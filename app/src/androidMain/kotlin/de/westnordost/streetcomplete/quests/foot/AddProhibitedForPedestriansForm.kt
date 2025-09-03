package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.ACTUALLY_HAS_SIDEWALK
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.NO
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_allowed
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_prohibited
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_sidewalk
import de.westnordost.streetcomplete.ui.common.TextItem

class AddProhibitedForPedestriansForm : AListQuestForm<ProhibitedForPedestriansAnswer>() {

    override val items = listOf(
        TextItem(YES, Res.string.quest_accessible_for_pedestrians_prohibited),
        TextItem(NO, Res.string.quest_accessible_for_pedestrians_allowed),
        TextItem(ACTUALLY_HAS_SIDEWALK, Res.string.quest_accessible_for_pedestrians_sidewalk),
    )
}
