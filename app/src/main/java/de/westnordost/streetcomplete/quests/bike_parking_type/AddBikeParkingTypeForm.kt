package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.BUILDING
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.HANDLEBAR_HOLDER
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.LOCKERS
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.SHED
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.STANDS
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.WALL_LOOPS
import de.westnordost.streetcomplete.view.image_select.Item

class AddBikeParkingTypeForm : AImageListQuestForm<BikeParkingType, BikeParkingType>() {

    override val items = listOf(
        Item(STANDS, R.drawable.bicycle_parking_type_stand, R.string.quest_bicycle_parking_type_stand),
        Item(WALL_LOOPS, R.drawable.bicycle_parking_type_wheelbenders, R.string.quest_bicycle_parking_type_wheelbender),
        Item(SHED, R.drawable.bicycle_parking_type_shed, R.string.quest_bicycle_parking_type_shed),
        Item(LOCKERS, R.drawable.bicycle_parking_type_lockers, R.string.quest_bicycle_parking_type_locker),
        Item(BUILDING, R.drawable.bicycle_parking_type_building, R.string.quest_bicycle_parking_type_building),
        Item(HANDLEBAR_HOLDER, R.drawable.bicycle_parking_type_handlebarholder, R.string.quest_bicycle_parking_type_handlebarholder)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BikeParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
