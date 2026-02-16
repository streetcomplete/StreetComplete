package de.westnordost.streetcomplete.data.osmcal


interface CalendarEventsSource {
    interface Listener {
        fun onUnreadCountChanged()
    }

    fun getUnreadCount(): Int
    fun getFirstUnread(): CalendarEvent?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
