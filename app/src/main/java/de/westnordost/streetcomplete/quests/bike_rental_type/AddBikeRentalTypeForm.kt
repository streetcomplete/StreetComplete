package de.westnordost.streetcomplete.quests.bike_rental_type

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DOCKING_STATION
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DROPOFF_POINT
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.HUMAN
import de.westnordost.streetcomplete.view.image_select.Item

class AddBikeRentalTypeForm : AImageListQuestAnswerFragment<BikeRentalType, BikeRentalType>() {

    override val items = listOf(
        Item(DOCKING_STATION, R.drawable.bicycle_rental_docking_station, R.string.quest_bicycle_rental_type_docking_station),
        Item(DROPOFF_POINT, R.drawable.bicycle_rental_dropoff_point, R.string.quest_bicycle_rental_type_dropoff_point),
        Item(HUMAN, R.drawable.bicycle_rental_human, R.string.quest_bicycle_rental_type_human),
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BikeRentalType>) {
        if (selectedItems.single() === HUMAN) confirmShop()
        else applyAnswer(selectedItems.single())
    }

    private fun confirmShop() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_bicycle_rental_type_bicycle_shop_confirmation)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(HUMAN) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
