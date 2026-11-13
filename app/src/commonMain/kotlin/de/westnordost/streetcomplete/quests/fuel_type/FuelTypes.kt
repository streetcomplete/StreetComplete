package de.westnordost.streetcomplete.quests.fuel_type

import kotlinx.serialization.Serializable

@Serializable
enum class FuelTypes(val value: String) {
    CNG("cng"),
    DIESEL("diesel"),
    E10("e10"),
    E85("e85"),
    LPG("lpg"),
    OCTANE_85("octane_85"),
    OCTANE_87("octane_87"),
    OCTANE_89("octane_89"),
    OCTANE_91("octane_91"),
    OCTANE_92("octane_92"),
    OCTANE_95("octane_95"),
    OCTANE_98("octane_98"),
}

fun parseAddFuelTypes(tags: Map<String, String>): Set<FuelTypes> {
    val initialSelectedItems = mutableSetOf<FuelTypes>()
    for (fuel in FuelTypes.entries) {
        if (tags["fuel:${fuel.value}"] == "yes") {
            initialSelectedItems += fuel
        }
    }
    return initialSelectedItems
}
