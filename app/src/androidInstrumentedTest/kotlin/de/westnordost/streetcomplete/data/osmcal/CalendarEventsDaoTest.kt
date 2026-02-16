package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class CalendarEventsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: CalendarEventsDao

    @BeforeTest fun createDao() {
        dao = CalendarEventsDao(database)
    }

    @Test
    fun `put and get`() {
        val tomorrow = Clock.System.now() + 1.days
        val multiDayEventTomorrow = CalendarEvent(
            id = 123L,
            name = "multi day event",
            startDate = tomorrow,
            endDate = tomorrow.plus(1.days),
            wholeDay = true,
            position = LatLon(5.0, 1.5),
            venue = "House",
            address = "an address",
            notified = false,
        )
        val eventSoon = CalendarEvent(
            id = 124L,
            name = "soon event",
            startDate = tomorrow + 1.hours,
            endDate = null,
            wholeDay = false,
            position = LatLon(-5.0, 2.5),
            venue = "House2",
            address = "an address2",
            notified = false,
        )
        val yesterdayEvent = CalendarEvent(
            id = 125L,
            name = "yesterday event",
            startDate = tomorrow - 2.days,
            endDate = null,
            wholeDay = false,
            position = LatLon(-5.0, 2.5),
            venue = "House3",
            address = "an address3",
            notified = false,
        )

        dao.putAll(listOf(multiDayEventTomorrow, eventSoon, yesterdayEvent))

        // getting / marking as read one by one
        assertEquals(2, dao.getUnreadCount())
        assertEquals(eventSoon, dao.getFirstUnread())

        dao.markRead(eventSoon.id)
        assertEquals(1, dao.getUnreadCount())
        assertEquals(multiDayEventTomorrow, dao.getFirstUnread())

        dao.markRead(multiDayEventTomorrow.id)
        assertEquals(0, dao.getUnreadCount())
        assertEquals(null, dao.getFirstUnread())

        // deleting yesterday's event
        assertEquals(1, dao.deleteOld())

        // new putAll doesn't overwrite existing records
        dao.putAll(listOf(multiDayEventTomorrow, eventSoon, yesterdayEvent))
        assertEquals(0, dao.getUnreadCount())
        assertEquals(null, dao.getFirstUnread())
    }
}
