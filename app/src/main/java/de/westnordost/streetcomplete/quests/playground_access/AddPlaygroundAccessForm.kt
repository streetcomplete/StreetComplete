package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.PRIVATE
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.YES

class AddPlaygroundAccessForm : AListQuestAnswerFragment<PlaygroundAccess>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_access_yes),
        TextItem(CUSTOMERS, R.string.quest_access_customers),
        TextItem(PRIVATE, R.string.quest_access_private),
    )
}
