package de.westnordost.streetcomplete.quests.bollard_type

sealed interface BollardTypeAnswer

enum class BollardType(val osmValue: String) : BollardTypeAnswer {
    FIXED("fixed"),
    FLEXIBLE("flexible"),
    RISING("rising"),
    REMOVABLE("removable"),
    FOLDABLE("foldable"),
}

data object BarrierTypeIsNotBollard : BollardTypeAnswer
