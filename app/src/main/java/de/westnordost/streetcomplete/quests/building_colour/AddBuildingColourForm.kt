package de.westnordost.streetcomplete.quests.building_colour

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddBuildingColourForm : AImageListQuestForm<BuildingColour, BuildingColour>() {

    override val items: List<DisplayItem<BuildingColour>>
        get() {
            val context = requireContext()
            return BuildingColour.values().map { it.asItem(context) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<BuildingColour>) {
        applyAnswer(selectedItems.single())
    }
}
