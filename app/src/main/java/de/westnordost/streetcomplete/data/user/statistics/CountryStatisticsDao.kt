package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTable.Columns.COUNTRY_CODE
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTable.Columns.RANK
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTable.NAME

/** Stores how many quests the user solved in which country */
class CountryStatisticsDao(private val db: Database) {

    fun getCountryWithBiggestSolvedCount(): CountryStatistics? =
        db.queryOne(NAME, orderBy = "$SUCCEEDED DESC") { it.toCountryStatistics() }

    fun getAll(): List<CountryStatistics> =
        db.query(NAME) { it.toCountryStatistics() }

    fun clear() {
        db.delete(NAME)
    }

    fun replaceAll(countriesStatistics: Collection<CountryStatistics>) {
        db.transaction {
            db.delete(NAME)
            if (countriesStatistics.isNotEmpty()) {
                db.replaceMany(NAME,
                    arrayOf(COUNTRY_CODE, SUCCEEDED, RANK),
                    countriesStatistics.map { arrayOf(it.countryCode, it.count, it.rank) }
                )
            }
        }
    }

    fun addOne(countryCode: String) {
        db.transaction {
            // first ensure the row exists
            db.insertOrIgnore(NAME, listOf(
                COUNTRY_CODE to countryCode,
                SUCCEEDED to 0
            ))

            // then increase by one
            db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
        }
    }

    fun subtractOne(countryCode: String) {
        db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
    }
}

private fun CursorPosition.toCountryStatistics() = CountryStatistics(
    getString(COUNTRY_CODE),
    getInt(SUCCEEDED),
    getIntOrNull(RANK)
)
