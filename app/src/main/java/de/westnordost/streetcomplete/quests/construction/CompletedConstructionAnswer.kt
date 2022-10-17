package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.util.ktx.LocalDate

sealed interface CompletedConstructionAnswer
data class StateAnswer(val value: Boolean) : CompletedConstructionAnswer
data class OpeningDateAnswer(val date: LocalDate) : CompletedConstructionAnswer
