package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths

sealed class OpeningHoursAnswer

data class RegularOpeningHours(val times:List<OpeningMonths>) : OpeningHoursAnswer()
object AlwaysOpen : OpeningHoursAnswer()
object NoOpeningHoursSign : OpeningHoursAnswer()
data class DescribeOpeningHours(val text:String) : OpeningHoursAnswer()
