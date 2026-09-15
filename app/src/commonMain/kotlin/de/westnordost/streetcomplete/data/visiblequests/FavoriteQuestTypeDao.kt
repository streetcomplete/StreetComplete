package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.FavoriteQuestTypeTable.Columns.EDIT_TYPE_PRESET_ID
import de.westnordost.streetcomplete.data.visiblequests.FavoriteQuestTypeTable.Columns.QUEST
import de.westnordost.streetcomplete.data.visiblequests.FavoriteQuestTypeTable.NAME
import de.westnordost.streetcomplete.util.Mockable

/** Stores which quest types have been set as favorites by the user */
@Mockable
class FavoriteQuestTypeDao(private val db: Database) {

    fun getAll(presetId: Long): List<String> =
        db.query(NAME,
            where = "$EDIT_TYPE_PRESET_ID = $presetId"
        ) { cursor ->
            cursor.getString(QUEST)
        }
    fun get(presetId: Long, quest: String): String? =
        db.queryOne(NAME,
            where = "$EDIT_TYPE_PRESET_ID = ? AND $QUEST = ?",
            args = arrayOf(presetId, quest)
        ) { cursor ->
            cursor.getStringOrNull(QUEST)
        }
    fun remove(presetId: Long, quest: String) {
        db.delete(
            NAME,
            where = "$EDIT_TYPE_PRESET_ID = ? AND $QUEST = ?",
            args = arrayOf(presetId, quest)
        )
    }

    fun put(presetId: Long, quest: String) {
        db.replace(NAME, listOf(
            EDIT_TYPE_PRESET_ID to presetId,
            QUEST to quest
        ))
    }
    fun putAll(presetId: Long, quests: Collection<String>) {
        db.transaction {
            clear(presetId)
            db.insertMany(
                NAME,
                columnNames = arrayOf(EDIT_TYPE_PRESET_ID, QUEST),
                valuesList = quests.map { arrayOf(presetId, it) }
            )
        }
    }
    fun clear(presetId: Long) {
        db.delete(NAME, where = "$EDIT_TYPE_PRESET_ID = $presetId")
    }
}
