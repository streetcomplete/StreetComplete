package de.westnordost.streetcomplete.data.changesets

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.changesets.OpenChangesetsTable.Columns.CHANGESET_ID
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsTable.NAME
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.queryOne

/** Keep track of changesets and the date of the last change that has been made to them  */
class OpenChangesetsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

	private val db get() = dbHelper.writableDatabase

    fun getAll(): Collection<OpenChangeset> {
	    return db.query(NAME) { it.createOpenChangesetInfo() }
    }

    fun put(openChangeset: OpenChangeset) {
	    db.insertWithOnConflict(NAME, null, openChangeset.createContentValues(), CONFLICT_REPLACE)
    }

    fun get(questType: String, source: String): OpenChangeset? {
        val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
        val args = arrayOf(questType, source)
        return db.queryOne(NAME, null, where, args) { it.createOpenChangesetInfo() }
    }

    fun delete(questType: String, source: String): Boolean {
	    val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
	    val whereArgs = arrayOf(questType, source)
        return db.delete(NAME, where, whereArgs) == 1
    }
}

private fun Cursor.createOpenChangesetInfo() =
	OpenChangeset(getString(QUEST_TYPE), getString(SOURCE), getLong(CHANGESET_ID))

private fun OpenChangeset.createContentValues() = contentValuesOf(
	QUEST_TYPE to questType,
	SOURCE to source,
	CHANGESET_ID to changesetId
)
