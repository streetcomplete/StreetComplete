package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList

sealed interface OpeningHoursAnswer

data class RegularOpeningHours(val hours: OpeningHoursRuleList) : OpeningHoursAnswer
data object AlwaysOpen : OpeningHoursAnswer
data class DescribeOpeningHours(val text: String) : OpeningHoursAnswer
data object NoOpeningHoursSign : OpeningHoursAnswer
