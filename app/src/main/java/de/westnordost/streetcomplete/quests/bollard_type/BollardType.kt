package de.westnordost.streetcomplete.quests.bollard_type

enum class BollardType(val osmValue: String) {
    RISING("rising"),
    REMOVABLE("removable"),
    FOLDABLE("foldable"),
    FLEXIBLE("flexible"),
    FIXED("fixed"),
    NOT_BOLLARD("not_bollard"),
}
