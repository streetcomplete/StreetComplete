package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.osm.Tags

enum class MemorialType(val osmValue: String, val osmMaterialValue: String? = null) {
    STATUE("statue"),
    BUST("bust"),
    PLAQUE("plaque"),
    WAR_MEMORIAL("war_memorial"),
    STONE("stone"),
    OBELISK("obelisk"),
    WOODEN_STELE("stele", "wood"),
    STONE_STELE("stele", "stone"),
    SCULPTURE("sculpture"),
}

fun MemorialType.applyTo(tags: Tags) {
    tags["memorial"] = this.osmValue
    if (this.osmMaterialValue != null) {
        tags["material"] = this.osmMaterialValue
    }
}
