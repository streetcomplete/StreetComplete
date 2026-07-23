package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import kotlinx.serialization.Serializable

sealed interface BicycleBarrierInstallationAnswer {
    data object NotBicycleBarrier : BicycleBarrierInstallationAnswer
}

@Serializable
enum class BicycleBarrierInstallation(val osmValue: String) : BicycleBarrierInstallationAnswer {
    FIXED("fixed"),
    OPENABLE("openable"),
    REMOVABLE("removable"),
}
