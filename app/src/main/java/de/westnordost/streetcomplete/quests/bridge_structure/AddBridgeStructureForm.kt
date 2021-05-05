package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddBridgeStructureForm : AImageListQuestAnswerFragment<BridgeStructure, BridgeStructure>() {

    // structures sorted highest to lowest amount of values on taginfo, footbridge-types last
    override val items = listOf(
        Item(BEAM, R.drawable.bridge_structure_beam, R.string.bridge_structure_beam_title, R.string.bridge_structure_beam_desc),
        Item(SUSPENSION, R.drawable.bridge_structure_suspension, R.string.bridge_structure_suspension_title, R.string.bridge_structure_suspension_desc),
        Item(ARCH, R.drawable.bridge_structure_arch, R.string.bridge_structure_arch_title),
        Item(TIED_ARCH, R.drawable.bridge_structure_tied_arch, R.string.bridge_structure_tied_arch_title),
        Item(TRUSS, R.drawable.bridge_structure_truss, R.string.bridge_structure_truss_title),
        Item(CABLE_STAYED, R.drawable.bridge_structure_cablestayed, R.string.bridge_structure_cablestayed_title, R.string.bridge_structure_cablestayed_desc),
        Item(HUMPBACK, R.drawable.bridge_structure_humpback, R.string.bridge_structure_humpback_title),
        Item(SIMPLE_SUSPENSION, R.drawable.bridge_structure_simple_suspension, R.string.bridge_structure_simple_suspension_title),
        Item(FLOATING, R.drawable.bridge_structure_floating, R.string.bridge_structure_floating_title, R.string.bridge_structure_floating_desc)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BridgeStructure>) {
        applyAnswer(selectedItems.first())
    }
}
