package de.westnordost.streetcomplete.quests.kerb_height

import kotlinx.serialization.Serializable

@Serializable
enum class KerbHeight(val osmValue: String) {
    RAISED("raised"),
    LOWERED("lowered"),
    FLUSH("flush"),
    KERB_RAMP("lowered"),
    NO_KERB("no"),
}
