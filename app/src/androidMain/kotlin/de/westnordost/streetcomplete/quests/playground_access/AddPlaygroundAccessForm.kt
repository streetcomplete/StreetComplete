package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.PRIVATE
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.YES
import de.westnordost.streetcomplete.ui.common.TextItem

class AddPlaygroundAccessForm : AListQuestForm<PlaygroundAccess>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_access_yes),
        TextItem(CUSTOMERS, R.string.quest_access_customers),
        TextItem(PRIVATE, R.string.quest_access_private),
    )
}
