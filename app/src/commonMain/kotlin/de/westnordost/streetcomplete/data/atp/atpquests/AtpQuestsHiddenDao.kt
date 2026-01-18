package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.atp.AtpQuestsHiddenTable.Columns.ATP_ENTRY_ID
import de.westnordost.streetcomplete.data.atp.AtpQuestsHiddenTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.atp.AtpQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Persists which atp ids should be hidden (because the user selected so) in the AllThePlaces quest */
class AtpQuestsHiddenDao(private val db: Database) {
    fun add(allThePlacesEntryId: Long) {
        db.insert(NAME, listOf(
            ATP_ENTRY_ID to allThePlacesEntryId,
            TIMESTAMP to nowAsEpochMilliseconds()
        ))
    }

    fun getTimestamp(allThePlacesEntryId: Long): Long? =
        db.queryOne(NAME, where = "$ATP_ENTRY_ID = $allThePlacesEntryId") { it.getLong(TIMESTAMP) }

    fun delete(allThePlacesEntryId: Long): Boolean =
        db.delete(NAME, where = "$ATP_ENTRY_ID = $allThePlacesEntryId") == 1

    fun getNewerThan(timestamp: Long): List<AtpQuestHiddenAt> =
        db.query(NAME, where = "$TIMESTAMP > $timestamp") { it.toAtpQuestHiddenAt() }

    fun getAll(): List<AtpQuestHiddenAt> =
        db.query(NAME) { it.toAtpQuestHiddenAt() }

    fun deleteAll(): Int =
        db.delete(NAME)

    fun countAll(): Int =
        db.queryOne(NAME, columns = arrayOf("COUNT(*)")) { it.getInt("COUNT(*)") } ?: 0
}

private fun CursorPosition.toAtpQuestHiddenAt() =
    AtpQuestHiddenAt(getLong(ATP_ENTRY_ID), getLong(TIMESTAMP))

data class AtpQuestHiddenAt(val allThePlacesEntryId: Long, val timestamp: Long)
