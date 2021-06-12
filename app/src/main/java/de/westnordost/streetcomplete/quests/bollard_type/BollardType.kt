package de.westnordost.streetcomplete.quests.bollard_type

enum class BollardType(val osmValue: String) {
    RISING("rising"),
    REMOVABLE_WITH_KEY("removable"),
    REMOVABLE_WITHOUT_KEY("removable"),
    FOLDABLE("foldable"),
    FLEXIBLE("flexible"),
    FIXED("fixed"),
}
