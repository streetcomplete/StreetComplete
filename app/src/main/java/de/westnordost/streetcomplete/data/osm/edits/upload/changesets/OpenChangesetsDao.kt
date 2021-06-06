package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.CHANGESET_ID
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable.NAME

/** Keep track of changesets and the date of the last change that has been made to them  */
class OpenChangesetsDao @Inject constructor(private val db: Database) {

    fun getAll(): Collection<OpenChangeset> =
        db.query(NAME) { it.toOpenChangeset() }

    fun put(openChangeset: OpenChangeset) {
        db.replace(NAME, openChangeset.toPairs())
    }

    fun get(questType: String, source: String): OpenChangeset? =
        db.queryOne(NAME,
            where = "$QUEST_TYPE = ? AND $SOURCE = ?",
            args = arrayOf(questType, source)
        ) { it.toOpenChangeset()  }

    fun delete(questType: String, source: String): Boolean =
        db.delete(NAME,
            where = "$QUEST_TYPE = ? AND $SOURCE = ?",
            args = arrayOf(questType, source)
        ) == 1
}

private fun OpenChangeset.toPairs() = listOf(
    QUEST_TYPE to questType,
    SOURCE to source,
    CHANGESET_ID to changesetId
)

private fun CursorPosition.toOpenChangeset() = OpenChangeset(
    getString(QUEST_TYPE),
    getString(SOURCE),
    getLong(CHANGESET_ID)
)
