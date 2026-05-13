package de.westnordost.streetcomplete.quests.construction

import kotlinx.datetime.LocalDate

sealed interface CompletedConstructionAnswer
data class ConstructionState(val value: Boolean) : CompletedConstructionAnswer
data class OpeningDate(val date: LocalDate) : CompletedConstructionAnswer
