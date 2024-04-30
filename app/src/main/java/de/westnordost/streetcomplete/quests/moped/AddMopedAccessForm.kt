package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.ALLOWED
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.DESIGNATED
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.FORBIDDEN

class AddMopedAccessForm : AListQuestForm<AddMopedAccessAnswer>() {

    override val items = listOf(
        TextItem(DESIGNATED, R.string.quest_moped_access_designated),
        TextItem(FORBIDDEN, R.string.quest_moped_access_forbidden),
        TextItem(ALLOWED, R.string.quest_moped_access_allowed)
    )
}
