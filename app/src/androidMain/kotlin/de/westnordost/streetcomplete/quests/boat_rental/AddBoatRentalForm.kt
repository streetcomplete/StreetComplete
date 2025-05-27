package de.westnordost.streetcomplete.quests.boat_rental

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBoatRentalForm : AImageListQuestForm<BoatRental, List<BoatRental>>() {

    override val items = BoatRental.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override val maxSelectableItems = -1
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<BoatRental>) {
        applyAnswer(selectedItems)
    }
}
