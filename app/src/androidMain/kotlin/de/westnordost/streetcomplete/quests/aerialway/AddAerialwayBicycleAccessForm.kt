package de.westnordost.streetcomplete.quests.aerialway

import android.os.Bundle
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddAerialwayBicycleAccessForm : AImageListQuestForm<AerialwayBicycleAccessAnswer, AerialwayBicycleAccessAnswer>() {

    override val items get() =
        AerialwayBicycleAccessAnswer.entries.map { it.asItem(requireContext()) }

    override val itemsPerRow = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClickOk(selectedItems: List<AerialwayBicycleAccessAnswer>) {
        applyAnswer(selectedItems.first())
    }
}
