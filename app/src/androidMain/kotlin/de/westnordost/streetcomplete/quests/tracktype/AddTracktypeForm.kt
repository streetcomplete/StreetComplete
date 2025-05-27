package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddTracktypeForm : AImageListQuestForm<Tracktype, Tracktype>() {

    override val items = Tracktype.entries.map { it.asItem() }

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<Tracktype>) {
        applyAnswer(selectedItems.single())
    }
}
