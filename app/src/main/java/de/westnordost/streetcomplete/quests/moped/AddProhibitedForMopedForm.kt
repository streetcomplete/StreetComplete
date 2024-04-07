package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.FORBIDDEN
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.ALLOWED
import de.westnordost.streetcomplete.quests.moped.ProhibitedForMopedAnswer.DESIGNATED



class AddProhibitedForMopedForm : AListQuestForm<ProhibitedForMopedAnswer>() {

    override val items = listOf(
        TextItem(DESIGNATED, R.string.quest_moped_prohibited_designated),
        TextItem(ALLOWED, R.string.quest_moped_prohibited_allowed) ,
        TextItem(FORBIDDEN, R.string.quest_moped_prohibited_forbidden)
    )
}
