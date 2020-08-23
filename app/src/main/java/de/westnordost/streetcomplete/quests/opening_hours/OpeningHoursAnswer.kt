package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningHoursRuleList

sealed class OpeningHoursAnswer

data class RegularOpeningHours(val hours: OpeningHoursRuleList) : OpeningHoursAnswer()
object AlwaysOpen : OpeningHoursAnswer()
data class DescribeOpeningHours(val text:String) : OpeningHoursAnswer()
object NoOpeningHoursSign : OpeningHoursAnswer()