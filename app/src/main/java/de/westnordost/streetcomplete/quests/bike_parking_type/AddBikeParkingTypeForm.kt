package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddBikeParkingTypeForm : AImageListQuestAnswerFragment<BikeParkingType, BikeParkingType>() {

    override val items = listOf(
        Item(STANDS, R.drawable.bicycle_parking_type_stand, R.string.quest_bicycle_parking_type_stand),
        Item(WALL_LOOPS, R.drawable.bicycle_parking_type_wheelbenders, R.string.quest_bicycle_parking_type_wheelbender),
        Item(SHED, R.drawable.bicycle_parking_type_shed, R.string.quest_bicycle_parking_type_shed),
        Item(LOCKERS, R.drawable.bicycle_parking_type_lockers, R.string.quest_bicycle_parking_type_locker),
        Item(BUILDING, R.drawable.bicycle_parking_type_building, R.string.quest_bicycle_parking_type_building)
    )

    override val itemsPerRow = 3

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes, ways with amenity = bicycle_parking").forEach {
            putMarker(it, R.drawable.ic_pin_bicycle_parking)
        }
    }

    override fun onClickOk(selectedItems: List<BikeParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
