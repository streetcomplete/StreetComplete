package de.westnordost.streetcomplete.data.user

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.Columns.COUNTRY_CODE
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable.NAME

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.*
import javax.inject.Singleton

/** Stores how many quests the user solved in which country*/
@Singleton class CountryStatisticsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): Map<String, Int> {
        return db.query(NAME) {
            it.getString(COUNTRY_CODE) to it.getInt(SUCCEEDED)
        }.toMap()
    }

    fun clear() {
        db.delete(NAME, null, null)
    }

    fun replaceAll(amounts: Map<String, Int>) {
        db.transaction {
            db.delete(NAME, null, null)
            for ((key, value) in amounts) {
                db.insert(NAME, null, contentValuesOf(
                    COUNTRY_CODE to key,
                    SUCCEEDED to value
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
