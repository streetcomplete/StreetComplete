package de.westnordost.streetcomplete.data.osmcal


object CalendarEventsTable {
    const val TABLE = "calendar_events"

    object Columns {
        const val ID = "id"
        const val NAME = "name"
        const val START_DATE = "start_date"
        const val END_DATE = "end_date"
        const val WHOLE_DAY = "whole_day"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val VENUE = "venue"
        const val ADDRESS = "address"
        const val READ = "read"
    }

    const val CREATE = """
        CREATE TABLE $TABLE (
            ${Columns.ID} LONG PRIMARY KEY
            ${Columns.NAME} TEXT NOT NULL,
            ${Columns.START_DATE} INTEGER NOT NULL,
            ${Columns.END_DATE} INTEGER,
            ${Columns.WHOLE_DAY} INTEGER NOT NULL,
            ${Columns.LATITUDE} DOUBLE NOT NULL,
            ${Columns.LONGITUDE} DOUBLE NOT NULL,
            ${Columns.VENUE} TEXT,
            ${Columns.ADDRESS} TEXT,
            ${Columns.READ} INTEGER NOT NULL,
        );
    """
}
