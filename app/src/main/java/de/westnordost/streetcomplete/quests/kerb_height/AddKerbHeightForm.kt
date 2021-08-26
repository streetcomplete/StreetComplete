package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment

class AddKerbHeightForm : AImageListQuestAnswerFragment<KerbHeight, KerbHeight>() {

    override val items = KerbHeight.values().toList().toItems()

    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<KerbHeight>) {
        applyAnswer(selectedItems.single())
    }
}
