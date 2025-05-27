package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddKerbHeightForm : AImageListQuestForm<KerbHeight, KerbHeight>() {

    override val items = KerbHeight.entries.map { it.asItem() }
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<KerbHeight>) {
        applyAnswer(selectedItems.single())
    }
}
