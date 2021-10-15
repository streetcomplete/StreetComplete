package de.westnordost.streetcomplete.quests.barrier_type

sealed interface StileTypeAnswer
object IsKissingGate : StileTypeAnswer

enum class StileType(val osmValue: String, val osmMaterialValue: String? = null): StileTypeAnswer {
    SQUEEZER("squeezer"),
    LADDER("ladder"),
    STEPOVER_WOODEN("stepover", "wood"),
    STEPOVER_STONE("stepover", "stone")
}
