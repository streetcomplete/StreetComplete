package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.util.Listeners

/** Controls calendar events pulled from osmcal.org*/
class CalendarEventsController(
    private val dao: CalendarEventsDao
): CalendarEventsSource {
    private val listeners = Listeners<CalendarEventsSource.Listener>()

    override fun getUnreadCount(): Int = dao.getUnreadCount()

    override fun getFirstUnread(): CalendarEvent? = dao.getFirstUnread()

    override fun addListener(listener: CalendarEventsSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: CalendarEventsSource.Listener) {
        listeners.remove(listener)
    }

    fun putAll(calendarEvents: Collection<CalendarEvent>) {
        dao.putAll(calendarEvents)
        // (well, potentially, at least)
        listeners.forEach { it.onUnreadCountChanged() }
    }

    fun markRead(id: Long) {
        dao.markRead(id)
        listeners.forEach { it.onUnreadCountChanged() }
    }

    fun deleteOld() { dao.deleteOld() }
}
