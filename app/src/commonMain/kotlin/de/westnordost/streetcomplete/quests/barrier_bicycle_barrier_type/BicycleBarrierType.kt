package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import kotlinx.serialization.Serializable

sealed interface BicycleBarrierTypeAnswer {
    data object NotBicycleBarrier : BicycleBarrierTypeAnswer
}

@Serializable
enum class BicycleBarrierType(val osmValue: String) : BicycleBarrierTypeAnswer {
    SINGLE("single"),
    DOUBLE("double"),
    TRIPLE("triple"),
    DIAGONAL("diagonal"),
    TILTED("tilted"),
}


