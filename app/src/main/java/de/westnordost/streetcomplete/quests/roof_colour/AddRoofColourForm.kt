package de.westnordost.streetcomplete.quests.roof_colour

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRoofColourForm : AImageListQuestForm<RoofColour, RoofColour>() {

    override val items get() = RoofColour.values().map { it.asItem(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<RoofColour>) {
        applyAnswer(selectedItems.single())
    }
}
