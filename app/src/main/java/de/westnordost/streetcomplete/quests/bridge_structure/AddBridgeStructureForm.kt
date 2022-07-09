package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.ARCH
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.BEAM
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.CABLE_STAYED
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.FLOATING
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.HUMPBACK
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.SIMPLE_SUSPENSION
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.SUSPENSION
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.TIED_ARCH
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.TRUSS
import de.westnordost.streetcomplete.view.image_select.Item

class AddBridgeStructureForm : AImageListQuestForm<BridgeStructure, BridgeStructure>() {

    // structures sorted highest to lowest amount of values on taginfo, footbridge-types last
    override val items = listOf(
        Item(BEAM, R.drawable.ic_bridge_structure_beam),
        Item(SUSPENSION, R.drawable.ic_bridge_structure_suspension),
        Item(ARCH, R.drawable.ic_bridge_structure_arch),
        Item(TIED_ARCH, R.drawable.ic_bridge_structure_tied_arch),
        Item(TRUSS, R.drawable.ic_bridge_structure_truss),
        Item(CABLE_STAYED, R.drawable.ic_bridge_structure_cablestayed),
        Item(HUMPBACK, R.drawable.ic_bridge_structure_humpback),
        Item(SIMPLE_SUSPENSION, R.drawable.ic_bridge_structure_simple_suspension),
        Item(FLOATING, R.drawable.ic_bridge_structure_floating)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BridgeStructure>) {
        applyAnswer(selectedItems.first())
    }
}
