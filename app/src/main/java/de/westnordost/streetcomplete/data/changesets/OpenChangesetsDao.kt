package de.westnordost.streetcomplete.data.changesets

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.ObjectRelationalMapping

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
class OpenChangesetsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OpenChangesetMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): Collection<OpenChangeset> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun put(openChangeset: OpenChangeset) {
        db.replaceOrThrow(NAME, null, mapping.toContentValues(openChangeset))
    }

    fun get(questType: String, source: String): OpenChangeset? {
        val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
        val args = arrayOf(questType, source)
        return db.queryOne(NAME, null, where, args) { mapping.toObject(it) }
    }

    fun delete(questType: String, source: String): Boolean {
        val where = "$QUEST_TYPE = ? AND $SOURCE = ?"
        val whereArgs = arrayOf(questType, source)
        return db.delete(NAME, where, whereArgs) == 1
    }
}

class OpenChangesetMapping @Inject constructor(): ObjectRelationalMapping<OpenChangeset> {

    override fun toContentValues(obj: OpenChangeset) = contentValuesOf(
        QUEST_TYPE to obj.questType,
        SOURCE to obj.source,
        CHANGESET_ID to obj.changesetId
    )

    override fun toObject(cursor: Cursor) = OpenChangeset(
        cursor.getString(QUEST_TYPE),
        cursor.getString(SOURCE),
        cursor.getLong(CHANGESET_ID)
    )
}
