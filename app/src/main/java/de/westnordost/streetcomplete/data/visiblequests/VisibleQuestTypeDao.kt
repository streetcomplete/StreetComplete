package de.westnordost.streetcomplete.data.visiblequests

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable.NAME
import de.westnordost.streetcomplete.ktx.getInt
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import javax.inject.Singleton

/** Stores which quest types are visible by user selection and which are not */
@Singleton class VisibleQuestTypeDao @Inject constructor(private val prefs: SharedPreferences, private val dbHelper: SQLiteOpenHelper) {

    /* Is a singleton because it has a in-memory cache that is synchronized with changes made on
       the DB */

    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry

    private val cache: MutableMap<String, Int> by lazy { loadQuestTypeVisibilities() }

    private val db get() = dbHelper.writableDatabase

    private fun loadQuestTypeVisibilities(): MutableMap<String, Int> {
        val result = mutableMapOf<String,Int>()
        db.query(NAME) { cursor ->
            val questTypeName = cursor.getString(QUEST_TYPE)
            result[questTypeName] = cursor.getInt(VISIBILITY)
        }
        return result
    }

    @Synchronized fun isVisible(questType: QuestType<*>): Boolean {
        val preset = prefs.getString(Prefs.QUEST_PRESET,null)?.toIntOrNull() ?: 0
        val questTypeName = questType.javaClass.simpleName
        return ((cache[questTypeName] ?: 0) shr preset) % 2 != 0 ?: (questType.defaultDisabledMessage <= 0)
    }

    @Synchronized fun setVisible(questType: QuestType<*>, visible: Boolean) {
        val preset = prefs.getString(Prefs.QUEST_PRESET,null)?.toIntOrNull() ?: 0
        val questTypeName = questType.javaClass.simpleName
        if (visible == isVisible(questType)) return
        val oldVis = cache[questTypeName] ?: 0
        val newVis =
            if ((oldVis shr preset) % 2 != 0)
                oldVis - (1 shl preset)
            else
                oldVis + (1 shl preset)
        db.replaceOrThrow(NAME, null, contentValuesOf(
            QUEST_TYPE to questTypeName,
            VISIBILITY to newVis
        ))
        cache[questTypeName] = newVis
    }

    @Synchronized fun clear() {
        for (questType in questTypeRegistry.all) {
            setVisible(questType, questType.defaultDisabledMessage <= 0)
        }
    }
}
