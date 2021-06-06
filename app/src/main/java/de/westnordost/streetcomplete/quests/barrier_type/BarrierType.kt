package de.westnordost.streetcomplete.quests.barrier_type

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
    STILE_STEPOVER("stile"),
    KISSING_GATE("kissing_gate"),
    BICYCLE_BARRIER("cycle_barrier")
}
