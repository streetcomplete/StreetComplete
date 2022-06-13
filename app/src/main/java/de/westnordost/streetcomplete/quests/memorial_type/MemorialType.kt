package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.data.osm.osmquests.Tags

sealed interface MemorialTypeAnswer

enum class MemorialType(val osmValue: String) : MemorialTypeAnswer {
    STATUE("statue"),
    BUST("bust"),
    PLAQUE("plaque"),
    WAR_MEMORIAL("war_memorial"),
    STONE("stone"),
    OBELISK("obelisk"),
}

enum class Stele(val osmMaterialValue: String) : MemorialTypeAnswer {
    WOODEN_STELE("wood"),
    STONE_STELE("stone"),
    KHACHKAR_STELE("stone"),
}

fun MemorialTypeAnswer.applyTo(tags: Tags) {
    when (this) {
        is MemorialType -> {
            tags["memorial"] = this.osmValue
        }
        is Stele -> {
            tags["memorial"] = "stele"
            tags["material"] = this.osmMaterialValue
        }
    }
}
