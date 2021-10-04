package de.westnordost.streetcomplete.quests.way_lit

sealed class WayLitOrIsStepsAnswer
object IsActuallyStepsAnswer : WayLitOrIsStepsAnswer()

data class WayLitAnswer(val value: WayLit) : WayLitOrIsStepsAnswer()
