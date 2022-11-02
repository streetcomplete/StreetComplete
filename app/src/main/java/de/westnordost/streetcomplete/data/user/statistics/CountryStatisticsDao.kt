package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTables.Columns.COUNTRY_CODE
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTables.Columns.RANK
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTables.Columns.SUCCEEDED

/** Stores how many quests the user solved in which country */
class CountryStatisticsDao(private val db: Database, private val name: String) {

    fun getCountryWithBiggestSolvedCount(): CountryStatistics? =
        db.queryOne(name, orderBy = "$SUCCEEDED DESC") { it.toCountryStatistics() }

    fun getAll(): List<CountryStatistics> =
        db.query(name) { it.toCountryStatistics() }

    fun clear() {
        db.delete(name)
    }

    fun replaceAll(countriesStatistics: Collection<CountryStatistics>) {
        db.transaction {
            db.delete(name)
            if (countriesStatistics.isNotEmpty()) {
                db.replaceMany(name,
                    arrayOf(COUNTRY_CODE, SUCCEEDED, RANK),
                    countriesStatistics.map { arrayOf(it.countryCode, it.count, it.rank) }
                )
            }
        }
    }

    fun addOne(countryCode: String) {
        db.transaction {
            // first ensure the row exists
            db.insertOrIgnore(name, listOf(
                COUNTRY_CODE to countryCode,
                SUCCEEDED to 0
            ))

            // then increase by one
            db.exec("UPDATE $name SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
        }
    }

    fun subtractOne(countryCode: String) {
        db.exec("UPDATE $name SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
    }
}

private fun CursorPosition.toCountryStatistics() = CountryStatistics(
    getString(COUNTRY_CODE),
    getInt(SUCCEEDED),
    getIntOrNull(RANK)
)
