package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.CHANGESET_ID
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.NAME
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.queryOne

/** Keep track of changesets and the date of the last change that has been made to them  */
class OpenChangesetsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): Collection<OpenChangeset> {
        return db.query(NAME) { it.toOpenChangeset() }
    }

    fun put(openChangeset: OpenChangeset) {
        db.replaceOrThrow(NAME, null, openChangeset.toContentValues())
    }

    fun get(questType: String, source: String): OpenChangeset? {
        val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
        val args = arrayOf(questType, source)
        return db.queryOne(NAME, null, where, args) { it.toOpenChangeset()  }
    }

    fun delete(questType: String, source: String): Boolean {
        val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
        val whereArgs = arrayOf(questType, source)
        return db.delete(NAME, where, whereArgs) == 1
    }
}

private fun OpenChangeset.toContentValues() = contentValuesOf(
    QUEST_TYPE to questType,
    SOURCE to source,
    CHANGESET_ID to changesetId
)

private fun Cursor.toOpenChangeset() = OpenChangeset(
    getString(QUEST_TYPE),
    getString(SOURCE),
    getLong(CHANGESET_ID)
)
