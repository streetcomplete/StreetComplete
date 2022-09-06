package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBridgeStructureForm : AImageListQuestForm<BridgeStructure, BridgeStructure>() {

    override val items = BridgeStructure.values().map { it.asItem() }
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BridgeStructure>) {
        applyAnswer(selectedItems.first())
    }
}
