package de.westnordost.streetcomplete.data.visiblequests

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable.NAME
import de.westnordost.streetcomplete.ktx.getInt
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.queryOne

/** Stores which quest types are visible by user selection and which are not */
class VisibleQuestTypeDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

    private val db get() = dbHelper.writableDatabase

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
        db.replaceOrThrow(NAME, null, contentValuesOf(
            QUEST_TYPE to questTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
    }

    fun get(questTypeName: String): Boolean {
        return db.queryOne(NAME, arrayOf(VISIBILITY), "$QUEST_TYPE = ?", arrayOf(questTypeName)) {
            it.getInt(0) != 0
        } ?: true
    }

    fun clear() {
        db.delete(NAME, null, null)
    }
}
