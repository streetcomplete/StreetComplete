package de.westnordost.streetcomplete.quests.boat_lock_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBoatLockTypeForm : AImageListQuestForm<BoatLockType, List<BoatLockType>>() {

    override val items = BoatLockType.entries.map { it.asItem() }
    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<BoatLockType>) {
        applyAnswer(selectedItems)
    }
}
