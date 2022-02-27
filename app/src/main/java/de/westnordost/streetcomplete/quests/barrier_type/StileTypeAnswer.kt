package de.westnordost.streetcomplete.quests.barrier_type

sealed interface StileTypeAnswer

enum class ConvertedStile(val newBarrier: String) : StileTypeAnswer {
    KISSING_GATE("kissing_gate"),
    PASSAGE("entrance"),
    GATE("gate"),
}

enum class StileType(val osmValue: String, val osmMaterialValue: String? = null) : StileTypeAnswer {
    SQUEEZER("squeezer"),
    LADDER("ladder"),
    STEPOVER_WOODEN("stepover", "wood"),
    STEPOVER_STONE("stepover", "stone")
}
