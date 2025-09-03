package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.PRIVATE
import de.westnordost.streetcomplete.quests.playground_access.PlaygroundAccess.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access_customers
import de.westnordost.streetcomplete.resources.quest_access_private
import de.westnordost.streetcomplete.resources.quest_access_yes
import de.westnordost.streetcomplete.ui.common.TextItem

class AddPlaygroundAccessForm : AListQuestForm<PlaygroundAccess>() {

    override val items = listOf(
        TextItem(YES, Res.string.quest_access_yes),
        TextItem(CUSTOMERS, Res.string.quest_access_customers),
        TextItem(PRIVATE, Res.string.quest_access_private),
    )
}
