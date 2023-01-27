package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_PRESET_ID
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.NAME

/** Stores which quest types are visible by user selection and which are not */
class VisibleQuestTypeDao(private val db: Database) {

    fun put(presetId: Long, questTypeName: String, visible: Boolean) {
        db.replace(NAME, listOf(
            QUEST_PRESET_ID to presetId,
            QUEST_TYPE to questTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
    }

    fun putAll(presetId: Long, questTypeVisibilities: Map<String, Boolean>) {
        db.replaceMany(NAME,
            arrayOf(QUEST_PRESET_ID, QUEST_TYPE, VISIBILITY),
            questTypeVisibilities.map { (questTypeName, visibility) ->
                arrayOf(presetId, questTypeName, if (visibility) 1 else 0)
            }
        )
    }

    fun get(presetId: Long, questTypeName: String): Boolean =
        db.queryOne(NAME,
            columns = arrayOf(VISIBILITY),
            where = "$QUEST_PRESET_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(presetId, questTypeName)
        ) { it.getInt(VISIBILITY) != 0 } ?: true

    fun getAll(presetId: Long): MutableMap<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        db.query(NAME, where = "$QUEST_PRESET_ID = $presetId") { cursor ->
            val questTypeName = cursor.getString(QUEST_TYPE)
            val visible = cursor.getInt(VISIBILITY) != 0
            result[questTypeName] = visible
        }
        return result
    }

    fun clear(presetId: Long) {
        db.delete(NAME, where = "$QUEST_PRESET_ID = $presetId")
    }
}
