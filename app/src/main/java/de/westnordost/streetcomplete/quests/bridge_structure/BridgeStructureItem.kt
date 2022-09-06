package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.R
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

fun BridgeStructure.asItem() = Item(this, iconResId)

private val BridgeStructure.iconResId: Int get() = when (this) {
    BEAM ->              R.drawable.ic_bridge_structure_beam
    SUSPENSION ->        R.drawable.ic_bridge_structure_suspension
    ARCH ->              R.drawable.ic_bridge_structure_arch
    TIED_ARCH ->         R.drawable.ic_bridge_structure_tied_arch
    TRUSS ->             R.drawable.ic_bridge_structure_truss
    CABLE_STAYED ->      R.drawable.ic_bridge_structure_cablestayed
    HUMPBACK ->          R.drawable.ic_bridge_structure_humpback
    SIMPLE_SUSPENSION -> R.drawable.ic_bridge_structure_simple_suspension
    FLOATING ->          R.drawable.ic_bridge_structure_floating
}
