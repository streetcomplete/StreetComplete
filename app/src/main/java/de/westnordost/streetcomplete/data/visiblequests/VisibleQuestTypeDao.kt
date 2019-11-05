package de.westnordost.streetcomplete.data.visiblequests

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.NAME
import de.westnordost.streetcomplete.ktx.getInt
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query

class VisibleQuestTypeDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

    private val cache: MutableMap<String, Boolean> by lazy { loadQuestTypeVisibilities() }

    private val db get() = dbHelper.writableDatabase

    private fun loadQuestTypeVisibilities(): MutableMap<String, Boolean> {
        val result = mutableMapOf<String,Boolean>()
        db.query(NAME) { cursor ->
            val questTypeName = cursor.getString(QUEST_TYPE)
            val visible = cursor.getInt(VISIBILITY) != 0
            result[questTypeName] = visible
        }
        return result
    }

    @Synchronized fun isVisible(questType: QuestType<*>): Boolean {
        val questTypeName = questType.javaClass.simpleName
        return cache[questTypeName] ?: (questType.defaultDisabledMessage <= 0)
    }

    @Synchronized fun setVisible(questType: QuestType<*>, visible: Boolean) {
        val questTypeName = questType.javaClass.simpleName
        db.replaceOrThrow(NAME, null, contentValuesOf(
            QUEST_TYPE to questTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
        cache[questTypeName] = visible
    }

    @Synchronized fun clear() {
        db.delete(NAME, null, null)
        cache.clear()
    }
}
