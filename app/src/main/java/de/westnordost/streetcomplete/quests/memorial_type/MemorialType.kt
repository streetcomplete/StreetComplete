package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.data.osm.osmquests.Tags

enum class MemorialTypeAnswer(val osmValue: String, val osmMaterialValue: String? = null) {
    STATUE("statue"),
    BUST("bust"),
    PLAQUE("plaque"),
    WAR_MEMORIAL("war_memorial"),
    STONE("stone"),
    OBELISK("obelisk"),
    WOODEN_STELE("stele", "wood"),
    STONE_STELE("stele", "stone"),
}

fun MemorialTypeAnswer.applyTo(tags: Tags) {
    tags["memorial"] = this.osmValue
    if (this.osmMaterialValue != null) {
        tags["material"] = this.osmMaterialValue
    }
}
