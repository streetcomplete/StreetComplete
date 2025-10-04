package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.osm.Tags

enum class BarrierType(val osmValue: String) {
    PASSAGE("entrance"),
    GATE("gate"),
    LIFT_GATE("lift_gate"),
    SWING_GATE("swing_gate"),
    BOLLARD("bollard"),
    CHAIN("chain"),
    ROPE("rope"),
    WIRE_GATE("hampshire_gate"),
    CATTLE_GRID("cattle_grid"),
    BLOCK("block"),
    JERSEY_BARRIER("jersey_barrier"),
    LOG("log"),
    KERB("kerb"),
    HEIGHT_RESTRICTOR("height_restrictor"),
    FULL_HEIGHT_TURNSTILE("full-height_turnstile"),
    TURNSTILE("turnstile"),
    DEBRIS_PILE("debris"),
    STILE_SQUEEZER("stile"),
    STILE_LADDER("stile"),
    STILE_STEPOVER_WOODEN("stile"),
    STILE_STEPOVER_STONE("stile"),
    KISSING_GATE("kissing_gate"),
    BICYCLE_BARRIER("cycle_barrier")
}

fun BarrierType.applyTo(tags: Tags) {
    tags["barrier"] = this.osmValue
    when (this) {
        BarrierType.STILE_SQUEEZER -> {
            tags["stile"] = "squeezer"
        }
        BarrierType.STILE_LADDER -> {
            tags["stile"] = "ladder"
        }
        BarrierType.STILE_STEPOVER_WOODEN -> {
            tags["stile"] = "stepover"
            tags["material"] = "wood"
        }
        BarrierType.STILE_STEPOVER_STONE -> {
            tags["stile"] = "stepover"
            tags["material"] = "stone"
        }
        else -> {
            // nothing
        }
    }
}
