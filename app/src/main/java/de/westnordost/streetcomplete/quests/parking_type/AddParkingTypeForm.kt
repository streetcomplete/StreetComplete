package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.LANE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.MULTI_STOREY
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.STREET_SIDE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.SURFACE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.UNDERGROUND
import de.westnordost.streetcomplete.view.image_select.Item

class AddParkingTypeForm : AImageListQuestForm<ParkingType, ParkingType>() {

    override val items = listOf(
        Item(SURFACE,      R.drawable.parking_type_surface,     R.string.quest_parkingType_surface),
        Item(STREET_SIDE,  R.drawable.parking_type_street_side, R.string.quest_parkingType_street_side),
        Item(LANE,         R.drawable.parking_type_lane,        R.string.quest_parkingType_lane),
        Item(UNDERGROUND,  R.drawable.parking_type_underground, R.string.quest_parkingType_underground),
        Item(MULTI_STOREY, R.drawable.parking_type_multistorey, R.string.quest_parkingType_multiStorage)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
