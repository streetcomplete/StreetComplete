package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.parking_access.ParkingAccess.*

class AddParkingAccessForm : AListQuestAnswerFragment<ParkingAccess>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_parking_access_yes),
        TextItem(PRIVATE, R.string.quest_parking_access_private),
        TextItem(CUSTOMERS, R.string.quest_parking_access_customers),
    )
}
