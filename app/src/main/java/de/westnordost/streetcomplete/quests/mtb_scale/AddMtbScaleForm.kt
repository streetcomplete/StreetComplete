package de.westnordost.streetcomplete.quests.mtb_scale

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddMtbScaleForm : AImageListQuestForm<MtbScale, MtbScale>() {

    override val items = MtbScale.values().map { it.asItem() }

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<MtbScale>) {
        applyAnswer(selectedItems.single())
    }
}
