package de.westnordost.streetcomplete.quests.general_ref

sealed interface GeneralRefAnswer

data class GeneralRef(val ref: String) : GeneralRefAnswer
object NoVisibleGeneralRef : GeneralRefAnswer
