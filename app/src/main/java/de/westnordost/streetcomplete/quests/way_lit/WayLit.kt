package de.westnordost.streetcomplete.quests.way_lit

sealed interface WayLitOrIsStepsAnswer
object IsActuallyStepsAnswer : WayLitOrIsStepsAnswer

enum class WayLit(val osmValue: String) : WayLitOrIsStepsAnswer {
    NIGHT_AND_DAY("24/7"),
    AUTOMATIC("automatic"),
    YES("yes"),
    NO("no"),
}
