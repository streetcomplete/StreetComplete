package de.westnordost.streetcomplete.osm.opening_hours.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface OpeningHoursRow
@Serializable
data class OpeningMonthsRow(var months: Months) : OpeningHoursRow
@Serializable
data class OpeningWeekdaysRow(var weekdays: Weekdays, var timeRange: TimeRange) : OpeningHoursRow
@Serializable
data class OffDaysRow(var weekdays: Weekdays) : OpeningHoursRow
