package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.NO
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.TERMINAL
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.WIFI
import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.WIRED
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_internet_access_no
import de.westnordost.streetcomplete.resources.quest_internet_access_terminal
import de.westnordost.streetcomplete.resources.quest_internet_access_wired
import de.westnordost.streetcomplete.resources.quest_internet_access_wlan
import de.westnordost.streetcomplete.ui.common.TextItem

class AddInternetAccessForm : AListQuestForm<InternetAccess>() {

    override val items = listOf(
        TextItem(WIFI, Res.string.quest_internet_access_wlan),
        TextItem(NO, Res.string.quest_internet_access_no),
        TextItem(TERMINAL, Res.string.quest_internet_access_terminal),
        TextItem(WIRED, Res.string.quest_internet_access_wired),
    )
}
