package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.AFTER
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.BEFORE
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.QUEST_PRESET_ID
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.NAME

class QuestTypeOrderDao(private val db: Database) {

    fun getAll(presetId: Long): List<Pair<String, String>> =
        db.query(NAME,
            where = "$QUEST_PRESET_ID = $presetId",
            orderBy = "ROWID ASC"
        ) { cursor ->
            cursor.getString(BEFORE) to cursor.getString(AFTER)
        }

    fun setAll(presetId: Long, pairs: List<Pair<String, String>>) {
        db.transaction {
            clear(presetId)
            db.insertMany(NAME,
                columnNames = arrayOf(QUEST_PRESET_ID, BEFORE, AFTER),
                valuesList = pairs.map { arrayOf(presetId, it.first, it.second) }
            )
        }
    }

    fun put(presetId: Long, pair: Pair<String, String>) {
        db.insert(NAME, listOf(
            QUEST_PRESET_ID to presetId,
            BEFORE to pair.first,
            AFTER to pair.second
        ))
    }

    fun clear(presetId: Long) {
        db.delete(NAME, where = "$QUEST_PRESET_ID = $presetId")
    }
}
