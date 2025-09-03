package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.DESIGNATED
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.FORBIDDEN
import de.westnordost.streetcomplete.quests.moped.AddMopedAccessAnswer.NO_SIGN
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_moped_access_allowed
import de.westnordost.streetcomplete.resources.quest_moped_access_designated
import de.westnordost.streetcomplete.resources.quest_moped_access_forbidden
import de.westnordost.streetcomplete.ui.common.TextItem

class AddMopedAccessForm : AListQuestForm<AddMopedAccessAnswer>() {

    override val items = listOf(
        TextItem(DESIGNATED, Res.string.quest_moped_access_designated),
        TextItem(FORBIDDEN, Res.string.quest_moped_access_forbidden),
        TextItem(NO_SIGN, Res.string.quest_moped_access_allowed)
    )
}
