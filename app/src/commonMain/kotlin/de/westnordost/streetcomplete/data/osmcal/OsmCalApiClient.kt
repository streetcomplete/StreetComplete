package de.westnordost.streetcomplete.data.osmcal

/** Talks with the OsmCal API
 *  https://osmcal.org/api/v2/ */
interface OsmCalApiClient {
    /** Get all events */
    suspend fun getEvents(): List<CalendarEvent>
}
