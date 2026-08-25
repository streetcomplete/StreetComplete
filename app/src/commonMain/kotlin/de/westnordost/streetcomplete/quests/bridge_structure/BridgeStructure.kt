package de.westnordost.streetcomplete.quests.bridge_structure

import kotlinx.serialization.Serializable

// structures sorted highest to lowest amount of values on taginfo, footbridge-types last
@Serializable
enum class BridgeStructure(val osmValue: String) {
    BEAM("beam"),
    SUSPENSION("suspension"),
    ARCH("arch"),
    TIED_ARCH("arch"), // a subtype of arch, but visually quite different
    TRUSS("truss"),
    CABLE_STAYED("cable-stayed"),
    HUMPBACK("humpback"),
    SIMPLE_SUSPENSION("simple-suspension"),
    FLOATING("floating"),
}
