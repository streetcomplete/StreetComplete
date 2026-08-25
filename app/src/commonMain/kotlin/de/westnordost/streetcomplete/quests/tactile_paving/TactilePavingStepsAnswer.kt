package de.westnordost.streetcomplete.quests.tactile_paving

enum class TactilePavingStepsAnswer(val osmValue: String) {
    YES("yes"),
    NO("no"),
    TOP("partial"),
    BOTTOM("incorrect")
}
