package de.westnordost.streetcomplete.data.achievements

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.achievements.UserLinksTable.Columns.LINK
import de.westnordost.streetcomplete.data.achievements.UserLinksTable.NAME

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.*

/** Stores which links have been unlocked by the user */
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
}
