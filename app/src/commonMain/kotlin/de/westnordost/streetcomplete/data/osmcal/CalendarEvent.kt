package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.datetime.Instant

data class CalendarEvent(
    val id: Long,
    val name: String,
    val startDate: Instant,
    val endDate: Instant?,
    val wholeDay: Boolean,
    val position: LatLon,
    val venue: String?,
    val address: String?,
    val notified: Boolean,
) {
    val url: String get() = "https://osmcal.org/event/$id/"
}
