package de.westnordost.streetcomplete.data.externalsource

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.Columns.EDIT_ID
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.Columns.ID
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.Columns.SOURCE
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.NAME_EDITS
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables.NAME_HIDDEN
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

class ExternalSourceDao(private val db: Database) {

    fun addElementEdit(key: ExternalSourceQuestKey, elementEditId: Long) {
        db.insert(NAME_EDITS, listOf(
            EDIT_ID to elementEditId,
            ID to key.id,
            SOURCE to key.source
        ))
    }

    fun getKeyForElementEdit(elementEditId: Long): ExternalSourceQuestKey? =
        db.queryOne(NAME_EDITS, where = "$EDIT_ID = $elementEditId") { it.toKey() }

    fun deleteElementEdit(elementEditId: Long) =
        db.delete(NAME_EDITS, where = "$EDIT_ID = $elementEditId") > 0

    fun deleteAllExceptForElementEdits(elementEditIds: Collection<Long>) =
        db.delete(NAME_EDITS, where = "$EDIT_ID not in (${elementEditIds.joinToString(",")})") > 0

    fun hide(key: ExternalSourceQuestKey): Long {
        val timestamp = nowAsEpochMilliseconds()
        val inserted = db.insert(NAME_HIDDEN, listOf(
            ID to key.id,
            SOURCE to key.source,
            TIMESTAMP to timestamp
        )) > 0
        return if (inserted) timestamp else 0L
    }

    fun getHiddenTimestamp(key: ExternalSourceQuestKey): Long? =
        db.queryOne(NAME_HIDDEN,
            where = "$ID = '${key.id}' AND $SOURCE = '${key.source}'",
            columns = arrayOf(TIMESTAMP)
        ) { it.getLong(TIMESTAMP) }

    fun unhide(key: ExternalSourceQuestKey) =
        db.delete(NAME_HIDDEN, where = "$ID = '${key.id}' AND $SOURCE = '${key.source}'") > 0

    fun getAllHiddenNewerThan(timestamp: Long): List<Pair<ExternalSourceQuestKey, Long>> =
        db.query(NAME_HIDDEN, where = "$TIMESTAMP > $timestamp") { it.toKey() to it.getLong(TIMESTAMP) }

    fun getAllHidden(): List<ExternalSourceQuestKey> =
        db.query(NAME_HIDDEN, columns = arrayOf(ID, SOURCE)) { it.toKey() }

    fun unhideAll() = db.delete(NAME_HIDDEN)
}

private fun CursorPosition.toKey() = ExternalSourceQuestKey(getString(ID), getString(SOURCE))

object ExternalSourceQuestTables {
    const val NAME_HIDDEN = "other_source_hidden"
    const val NAME_EDITS = "other_source_edits"

    object Columns {
        const val ID = "id"
        const val SOURCE = "source"
        const val TIMESTAMP = "timestamp" // hidden only
        const val EDIT_ID = "edit_id" // edits only
    }

    const val CREATE_HIDDEN = """
        CREATE TABLE IF NOT EXISTS $NAME_HIDDEN (
            $ID TEXT,
            $SOURCE TEXT,
            $TIMESTAMP int NOT NULL,
            PRIMARY KEY (
                $ID,
                $SOURCE
            )
        );
    """

    const val CREATE_EDITS = """
        CREATE TABLE IF NOT EXISTS $NAME_EDITS (
            $EDIT_ID INTEGER PRIMARY KEY,
            $ID TEXT,
            $SOURCE TEXT
        );
    """
}
