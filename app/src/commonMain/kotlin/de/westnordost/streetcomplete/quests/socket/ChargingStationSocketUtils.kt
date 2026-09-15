package de.westnordost.streetcomplete.quests.socket

val INVALID_CHARGING_STATION_SOCKETS = listOf(
    // not a socket but brand. Actual sockets differ per region
    "tesla", "tesla_standard", "tesla_supercharger_ccs", "tesla_destination", "tesla_supercharger",
    // not a socket but a brand. Use type2*
    "Mennekes",
    // SCAME used to produce type3c and type3a
    "type3", "scame", "type3A", "type3C",
    // there are two types of CCS, they are tagged type1_combo / type2_combo
    // (https://en.wikipedia.org/wiki/Combined_Charging_System)
    "ccs", "ccs1", "ccs2", "type2_ccs", "type1_ccs",
    // another name for the type1
    // (https://en.wikipedia.org/wiki/SAE_J1772)
    "j1772",
    // these types always come with a cable (or in case of type3: distinction not worth making)
    "type1_cable", "type2_combo_cable", "type3a_cable",
    // GB/T is the standard name, which includes several plugs
    // (https://en.wikipedia.org/wiki/GB/T_charging_standard)
    "gbt",
    // yes, which type?
    "type",
    // these refer to the configurations used for DC charging designated in IEC 62196
    // (https://en.wikipedia.org/wiki/IEC_62196#Configurations_2)
    "type_ee", // type1_combo
    "type_ff", // type2_combo
    "type_aa", // chademo
    "type_bb", // gb_dc
)
