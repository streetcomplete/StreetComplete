package de.westnordost.streetcomplete.data.osmcal


interface CalendarEventsSource {
    interface Listener {
        fun onUnreadCountChanged()
    }

    /** for how many calendar events the user didn't see the message yet */
    fun getUnreadCount(): Int
    /** get the earliest unread calendar event or null if there is none */
    fun getFirstUnread(): CalendarEvent?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
