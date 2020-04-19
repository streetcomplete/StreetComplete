package de.westnordost.streetcomplete.data.user.achievements

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable.Columns.LINK
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable.NAME

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.*

/** Stores which link ids have been unlocked by the user */
class UserLinksDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): List<String> {
        return db.query(NAME) { it.getString(LINK) }
    }

    fun clear() {
        db.delete(NAME, null, null)
    }

    fun add(link: String) {
        db.insertWithOnConflict(NAME, null, contentValuesOf(LINK to link), CONFLICT_IGNORE)
    }

    fun addAll(links: List<String>): Int {
        var addedRows = 0
        db.transaction {
            for (link in links) {
                val rowId = db.insertWithOnConflict(NAME, null, contentValuesOf(LINK to link), CONFLICT_IGNORE)
                if (rowId != -1L) addedRows++
            }
        }
        return addedRows
    }
}
