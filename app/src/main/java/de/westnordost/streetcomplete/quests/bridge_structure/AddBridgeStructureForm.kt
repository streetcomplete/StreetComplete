package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddBridgeStructureForm : AImageListQuestAnswerFragment<String, String>() {

    // structures sorted highest to lowest amount of values on taginfo, footbridge-types last
    override val items = listOf(
        Item("beam", R.drawable.bridge_structure_beam),
        Item("suspension", R.drawable.bridge_structure_suspension),
        Item("arch", R.drawable.bridge_structure_arch),
        Item("arch", R.drawable.bridge_structure_tied_arch), // a subtype of arch, but visually quite different
        Item("truss", R.drawable.bridge_structure_truss),
        Item("cable-stayed", R.drawable.bridge_structure_cablestayed),
        Item("humpback", R.drawable.bridge_structure_humpback),
        Item("simple-suspension", R.drawable.bridge_structure_simple_suspension),
        Item("floating", R.drawable.bridge_structure_floating)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.first())
    }
}
