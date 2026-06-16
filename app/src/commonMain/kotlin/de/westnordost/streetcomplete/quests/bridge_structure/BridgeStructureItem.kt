package de.westnordost.streetcomplete.quests.bridge_structure

import de.westnordost.streetcomplete.quests.bridge_structure.BridgeStructure.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

val BridgeStructure.icon: DrawableResource get() = when (this) {
    BEAM ->              Res.drawable.bridge_structure_beam
    SUSPENSION ->        Res.drawable.bridge_structure_suspension
    ARCH ->              Res.drawable.bridge_structure_arch
    TIED_ARCH ->         Res.drawable.bridge_structure_tied_arch
    TRUSS ->             Res.drawable.bridge_structure_truss
    CABLE_STAYED ->      Res.drawable.bridge_structure_cablestayed
    HUMPBACK ->          Res.drawable.bridge_structure_humpback
    SIMPLE_SUSPENSION -> Res.drawable.bridge_structure_simple_suspension
    FLOATING ->          Res.drawable.bridge_structure_floating
}
