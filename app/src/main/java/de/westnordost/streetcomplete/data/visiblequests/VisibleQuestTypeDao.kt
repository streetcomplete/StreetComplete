package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database

import javax.inject.Inject

import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.NAME

/** Stores which quest types are visible by user selection and which are not */
class VisibleQuestTypeDao @Inject constructor(private val db: Database) {

    fun getAll(): MutableMap<String, Boolean> {
        val result = mutableMapOf<String,Boolean>()
        db.query(NAME) { cursor ->
            val questTypeName = cursor.getString(QUEST_TYPE)
            val visible = cursor.getInt(VISIBILITY) != 0
            result[questTypeName] = visible
        }
        return result
    }

    fun put(questTypeName: String, visible: Boolean) {
        db.replace(NAME, listOf(
            QUEST_TYPE to questTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
    }

    fun get(questTypeName: String): Boolean =
        db.queryOne(NAME,
            columns = arrayOf(VISIBILITY),
            where = "$QUEST_TYPE = ?",
            args = arrayOf(questTypeName)
        ) { it.getInt(VISIBILITY) != 0 } ?: true

    fun clear() {
        db.delete(NAME)
    }
}
