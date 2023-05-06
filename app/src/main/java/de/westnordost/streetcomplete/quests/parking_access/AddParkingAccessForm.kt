package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.PRIVATE
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.YES

class AddParkingAccessForm : AListQuestForm<ParkingAccess>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_access_yes),
        TextItem(CUSTOMERS, R.string.quest_access_customers),
        TextItem(PRIVATE, R.string.quest_access_private),
    )
}
