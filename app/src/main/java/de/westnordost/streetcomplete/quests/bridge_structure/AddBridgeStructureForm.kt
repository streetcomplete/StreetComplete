package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddBridgeStructureForm : AImageListQuestComposeForm<BridgeStructure, BridgeStructure>() {

    override val items = BridgeStructure.entries.map { it.asItem() }
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BridgeStructure>) {
        applyAnswer(selectedItems.first())
    }
}
