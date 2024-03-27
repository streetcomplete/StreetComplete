package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.quests.AImageListQuestForm
class AddParkingOrientationForm : AImageListQuestForm<ParkingOrientation, ParkingOrientation>() {
    override val items get() = ParkingOrientation.values().map { it.asItem(requireContext(), element.tags["parking"]) }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ParkingOrientation>) {
        applyAnswer(selectedItems.single())
    }
}
