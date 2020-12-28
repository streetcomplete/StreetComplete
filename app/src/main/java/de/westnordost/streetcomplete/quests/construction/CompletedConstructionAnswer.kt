package de.westnordost.streetcomplete.quests.construction

import java.util.*

sealed class CompletedConstructionAnswer
data class StateAnswer(val value : Boolean) : CompletedConstructionAnswer()
data class OpeningDateAnswer(val date: Date) : CompletedConstructionAnswer()
