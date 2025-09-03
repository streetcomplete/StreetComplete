package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.PRIVATE
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_access_customers
import de.westnordost.streetcomplete.resources.quest_access_private
import de.westnordost.streetcomplete.resources.quest_access_yes
import de.westnordost.streetcomplete.ui.common.TextItem

class AddParkingAccessForm : AListQuestForm<ParkingAccess>() {

    override val items = listOf(
        TextItem(YES, Res.string.quest_access_yes),
        TextItem(CUSTOMERS, Res.string.quest_access_customers),
        TextItem(PRIVATE, Res.string.quest_access_private),
    )
}
