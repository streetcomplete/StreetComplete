package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.TABLE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.ID
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.NAME
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.START_DATE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.END_DATE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.WHOLE_DAY
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.VENUE
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.ADDRESS
import de.westnordost.streetcomplete.data.osmcal.CalendarEventsTable.Columns.READ
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.datetime.Instant

class CalendarEventsDao(private val db: Database) {

    fun getFirstUnread(): CalendarEvent? {
        val now = nowAsEpochMilliseconds()
        return db.queryOne(
            table = TABLE,
            where = "$START_DATE > $now AND $READ = 0",
            orderBy = "$START_DATE ASC",
        ) {
            it.toCalendarEvent()
        }
    }

    fun getUnreadCount(): Int {
        val now = nowAsEpochMilliseconds()
        return db.queryOne(
            table = TABLE,
            columns = arrayOf("COUNT(*) as count"),
            where = "$START_DATE > $now AND $READ = 0",
        ) { it.getInt("count") } ?: 0
    }

    fun putAll(calendarEvents: Collection<CalendarEvent>) {
        db.insertOrIgnoreMany(
            table = TABLE,
            columnNames = arrayOf(
                ID,
                NAME,
                START_DATE,
                END_DATE,
                WHOLE_DAY,
                LATITUDE,
                LONGITUDE,
                VENUE,
                ADDRESS,
                READ
            ),
            valuesList = calendarEvents.map { arrayOf(
                it.id,
                it.name,
                it.startDate.toEpochMilliseconds(),
                it.endDate?.toEpochMilliseconds(),
                if (it.wholeDay) 1 else 0,
                it.position.latitude,
                it.position.longitude,
                it.venue,
                it.address,
                0
            ) }
        )
    }

    fun deleteOld(): Int {
        val now = nowAsEpochMilliseconds()
        return db.delete(
            table = TABLE,
            where = "$START_DATE < $now"
        )
    }

    fun markRead(id: Long) {
        db.update(
            table = TABLE,
            values = listOf(READ to 1),
            where = "$ID = $id",
        )
    }
}

private fun CursorPosition.toCalendarEvent() = CalendarEvent(
    id = getLong(ID),
    name = getString(NAME),
    startDate = Instant.fromEpochMilliseconds(getLong(START_DATE)),
    endDate = getLongOrNull(END_DATE)?.let { Instant.fromEpochMilliseconds(it) },
    wholeDay = getInt(WHOLE_DAY) != 0,
    position = LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
    venue = getStringOrNull(VENUE),
    address = getStringOrNull(ADDRESS),
    notified = getInt(READ) != 0
)
