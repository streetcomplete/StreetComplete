package de.westnordost.streetcomplete.quests.sac_scale

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.via_ferrata_scale.ViaFerrataScale
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddSacScaleForm : AImageListQuestForm<SacScale, SacScale>() {

    override val items: List<DisplayItem<SacScale>> get() = SacScale.entries.toItems()

    override val itemsPerRow = 1

    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select_sac_scale
    }

    override fun onClickOk(selectedItems: List<SacScale>) {
        applyAnswer(selectedItems.first())
    }
}
