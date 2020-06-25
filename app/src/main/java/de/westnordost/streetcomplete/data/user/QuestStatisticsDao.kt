package de.westnordost.streetcomplete.data.user

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.NAME
import de.westnordost.streetcomplete.ktx.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores how many quests of which quest types the user solved */
@Singleton class QuestStatisticsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedOne(questType: String)
        fun onSubtractedOne(questType: String)
        fun onReplacedAll()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getTotalAmount(): Int {
        return db.queryOne(NAME, arrayOf("total($SUCCEEDED)")) { it.getInt(0) } ?: 0
    }

    fun getAll(): Map<String, Int> {
        return db.query(NAME) {
            it.getString(QUEST_TYPE) to it.getInt(SUCCEEDED)
        }.toMap()
    }

    fun clear() {
        db.delete(NAME, null, null)
        listeners.forEach { it.onReplacedAll() }
    }

    fun replaceAll(amounts: Map<String, Int>) {
        db.transaction {
            db.delete(NAME, null, null)
            for ((key, value) in amounts) {
                db.insert(NAME, null, contentValuesOf(
                    QUEST_TYPE to key,
                    SUCCEEDED to value
                ))
            }
        }
        listeners.forEach { it.onReplacedAll() }
    }

    fun addOne(questType: String) {
        // first ensure the row exists
        db.insertWithOnConflict(NAME, null, contentValuesOf(
            QUEST_TYPE to questType,
            SUCCEEDED to 0
        ), CONFLICT_IGNORE)

        // then increase by one
        db.execSQL("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $QUEST_TYPE = ?", arrayOf(questType))
        listeners.forEach { it.onAddedOne(questType) }
    }

    fun subtractOne(questType: String) {
        db.execSQL("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $QUEST_TYPE = ?", arrayOf(questType))
        listeners.forEach { it.onSubtractedOne(questType) }
    }

    fun getAmount(questType: String): Int {
        return db.queryOne(NAME, arrayOf(SUCCEEDED), "$QUEST_TYPE = ?", arrayOf(questType)) {
            it.getInt(0)
        } ?: 0
    }

    fun getAmount(questTypes: List<String>): Int {
        val questionMarks = Array(questTypes.size) { "?" }.joinToString(",")
        val query = "$QUEST_TYPE in ($questionMarks)"
        return db.queryOne(NAME, arrayOf("total($SUCCEEDED)"), query, questTypes.toTypedArray()) {
            it.getInt(0)
        } ?: 0
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}
