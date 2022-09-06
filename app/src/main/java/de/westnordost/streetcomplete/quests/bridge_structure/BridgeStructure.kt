package de.westnordost.streetcomplete.quests.bridge_structure

// structures sorted highest to lowest amount of values on taginfo, footbridge-types last
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
