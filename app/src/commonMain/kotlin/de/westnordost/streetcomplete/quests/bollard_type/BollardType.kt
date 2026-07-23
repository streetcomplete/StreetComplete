package de.westnordost.streetcomplete.quests.bollard_type

import kotlinx.serialization.Serializable

sealed interface BollardTypeAnswer {
    data object NotBollard : BollardTypeAnswer
}

@Serializable
enum class BollardType(val osmValue: String) : BollardTypeAnswer {
    FIXED("fixed"),
    FLEXIBLE("flexible"),
    RISING("rising"),
    REMOVABLE("removable"),
    FOLDABLE("foldable"),
}


