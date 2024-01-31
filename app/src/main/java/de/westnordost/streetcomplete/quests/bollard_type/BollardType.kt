package de.westnordost.streetcomplete.quests.bollard_type

sealed interface BollardTypeAnswer

enum class BollardType(val osmValue: String) : BollardTypeAnswer {
    RISING("rising"),
    REMOVABLE("removable"),
    FOLDABLE("foldable"),
    FLEXIBLE("flexible"),
    FIXED("fixed"),
}

data object BarrierTypeIsNotBollard : BollardTypeAnswer
