package de.westnordost.streetcomplete.quests.construction

import java.time.LocalDate

sealed interface CompletedConstructionAnswer
data class StateAnswer(val value: Boolean) : CompletedConstructionAnswer
data class OpeningDateAnswer(val date: LocalDate) : CompletedConstructionAnswer
