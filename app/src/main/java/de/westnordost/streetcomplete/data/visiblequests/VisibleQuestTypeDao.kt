package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_PROFILE_ID

import javax.inject.Inject

import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.NAME

/** Stores which quest types are visible by user selection and which are not */
class VisibleQuestTypeDao @Inject constructor(private val db: Database) {

    fun getAll(profileId: Long): MutableMap<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        db.query(NAME, where = "$QUEST_PROFILE_ID = $profileId") { cursor ->
            val questTypeName = cursor.getString(QUEST_TYPE)
            val visible = cursor.getInt(VISIBILITY) != 0
            result[questTypeName] = visible
        }
        return result
    }

    fun put(profileId: Long, questTypeName: String, visible: Boolean) {
        db.replace(NAME, listOf(
            QUEST_PROFILE_ID to profileId,
            QUEST_TYPE to questTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
    }

    fun get(profileId: Long, questTypeName: String): Boolean =
        db.queryOne(NAME,
            columns = arrayOf(VISIBILITY),
            where = "$QUEST_PROFILE_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(profileId, questTypeName)
        ) { it.getInt(VISIBILITY) != 0 } ?: true

    fun clear(profileId: Long) {
        db.delete(NAME, where = "$QUEST_PROFILE_ID = $profileId")
    }
}
