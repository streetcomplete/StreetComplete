package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow

sealed class OpeningHoursAnswer

data class RegularOpeningHours(val times:List<OpeningMonthsRow>) : OpeningHoursAnswer()
object AlwaysOpen : OpeningHoursAnswer()
object NoOpeningHoursSign : OpeningHoursAnswer()
data class DescribeOpeningHours(val text:String) : OpeningHoursAnswer()
object UnmodifiedOpeningHours : OpeningHoursAnswer()
