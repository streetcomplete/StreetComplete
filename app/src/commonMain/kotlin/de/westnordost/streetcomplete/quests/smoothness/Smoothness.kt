package de.westnordost.streetcomplete.quests.smoothness

enum class Smoothness(val osmValue: String) {
    EXCELLENT("excellent"),
    GOOD("good"),
    INTERMEDIATE("intermediate"),
    BAD("bad"),
    VERY_BAD("very_bad"),
    HORRIBLE("horrible"),
    VERY_HORRIBLE("very_horrible"),
    IMPASSABLE("impassable"),
}
