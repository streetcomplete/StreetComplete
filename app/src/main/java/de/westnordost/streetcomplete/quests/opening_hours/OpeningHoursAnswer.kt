package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningHours

sealed class OpeningHoursAnswer

data class HasOpeningHours(val openingHours: OpeningHours) : OpeningHoursAnswer()
object NoOpeningHoursSign : OpeningHoursAnswer()
