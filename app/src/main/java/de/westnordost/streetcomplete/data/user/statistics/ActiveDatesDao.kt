package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.statistics.ActiveDaysTable.Columns.DATE
import de.westnordost.streetcomplete.data.user.statistics.ActiveDaysTable.NAME
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Stores the dates at which the user added edits */
class ActiveDatesDao(private val db: Database) {

    fun replaceAll(dates: List<LocalDate>) {
        db.transaction {
            db.delete(NAME)
            db.replaceMany(NAME, arrayOf(DATE), dates.map { arrayOf(it.toString()) })
        }
    }

    fun getAll(days: Int): List<LocalDate> =
        db.query(NAME, where = "$DATE >= date('now','-$days days')") {
            LocalDate.parse(it.getString(DATE))
        }

    fun clear() {
        db.delete(NAME)
    }

    fun addToday() {
        val currentDateUTC = systemTimeNow().toLocalDateTime(TimeZone.UTC).date
        db.insertOrIgnore(NAME, listOf(DATE to currentDateUTC.toString()))
    }
}
