package de.westnordost.streetcomplete.data.user

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.Columns.COUNTRY_CODE
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.Columns.RANK
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.NAME

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.*
import javax.inject.Singleton

/** Stores how many quests the user solved in which country */
@Singleton class CountryStatisticsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun getCountryWithBiggestSolvedCount(): CountryStatistics? {
        return db.queryOne(NAME, orderBy = "$SUCCEEDED DESC") {
            CountryStatistics(it.getString(COUNTRY_CODE), it.getInt(SUCCEEDED), it.getIntOrNull(RANK))
        }
    }

    fun getAll(): List<CountryStatistics> {
        return db.query(NAME) {
            CountryStatistics(it.getString(COUNTRY_CODE), it.getInt(SUCCEEDED), it.getIntOrNull(RANK))
        }
    }

    fun clear() {
        db.delete(NAME, null, null)
    }

    fun replaceAll(countriesStatistics: Collection<CountryStatistics>) {
        db.transaction {
            db.delete(NAME, null, null)
            for (statistics in countriesStatistics) {
                db.insert(NAME, null, contentValuesOf(
                    COUNTRY_CODE to statistics.countryCode,
                    SUCCEEDED to statistics.solvedCount,
                    RANK to statistics.rank
                ))
            }
        }
    }

    fun addOne(countryCode: String) {
        // first ensure the row exists
        db.insertWithOnConflict(NAME, null, contentValuesOf(
            COUNTRY_CODE to countryCode,
            SUCCEEDED to 0
        ), CONFLICT_IGNORE)

        // then increase by one
        db.execSQL("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
    }

    fun subtractOne(countryCode: String) {
        db.execSQL("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $COUNTRY_CODE = ?", arrayOf(countryCode))
    }
}