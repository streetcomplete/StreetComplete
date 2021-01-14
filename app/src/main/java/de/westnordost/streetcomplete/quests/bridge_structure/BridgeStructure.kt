package de.westnordost.streetcomplete.quests.bridge_structure

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
