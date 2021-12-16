package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.*

class AddInternetAccessForm : AListQuestAnswerFragment<InternetAccess>() {

    override val items = listOf(
        TextItem(WIFI, R.string.quest_internet_access_wlan),
        TextItem(NO, R.string.quest_internet_access_no),
        TextItem(TERMINAL, R.string.quest_internet_access_terminal),
        TextItem(WIRED, R.string.quest_internet_access_wired),
    )
}
