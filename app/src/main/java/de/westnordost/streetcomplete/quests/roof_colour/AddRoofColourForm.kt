package de.westnordost.streetcomplete.quests.roof_colour

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddRoofColourForm : AImageListQuestForm<RoofColour, RoofColour>() {

    override val items: List<DisplayItem<RoofColour>>
        get() {
        val context = requireContext()
        val shape = element.tags["roof:shape"];
        val roofShape = RoofShape.values().firstOrNull{ it.osmValue == shape }
        return RoofColour.values().map { it.asItem(context, roofShape) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<RoofColour>) {
        applyAnswer(selectedItems.single())
    }
}
