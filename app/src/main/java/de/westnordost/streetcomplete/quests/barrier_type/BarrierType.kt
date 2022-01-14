package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

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
    KERB("curb"),
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

fun BarrierType.applyTo(changes: StringMapChangesBuilder) {
    changes.addOrModify("barrier", this.osmValue)
    when (this) {
        BarrierType.STILE_SQUEEZER -> {
            changes.addOrModify("stile", "squeezer")
        }
        BarrierType.STILE_LADDER -> {
            changes.addOrModify("stile", "ladder")
        }
        BarrierType.STILE_STEPOVER_WOODEN -> {
            changes.addOrModify("stile", "stepover")
            changes.addOrModify("material", "wood")
        }
        BarrierType.STILE_STEPOVER_STONE -> {
            changes.addOrModify("stile", "stepover")
            changes.addOrModify("material", "stone")
        }
    }
}
