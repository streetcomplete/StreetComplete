package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.osm_opening_hours.model.OpeningHours

sealed interface OpeningHoursAnswer

data class RegularOpeningHours(val hours: OpeningHours) : OpeningHoursAnswer
data object AlwaysOpen : OpeningHoursAnswer
data class DescribeOpeningHours(val text: String) : OpeningHoursAnswer
data object NoOpeningHoursSign : OpeningHoursAnswer
