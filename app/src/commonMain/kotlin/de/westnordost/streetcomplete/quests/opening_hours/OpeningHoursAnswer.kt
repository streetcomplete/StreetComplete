package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours

sealed interface OpeningHoursAnswer

data class RegularOpeningHours(val hours: HierarchicOpeningHours) : OpeningHoursAnswer
data object AlwaysOpen : OpeningHoursAnswer
data class DescribeOpeningHours(val text: String) : OpeningHoursAnswer
data object NoOpeningHoursSign : OpeningHoursAnswer
