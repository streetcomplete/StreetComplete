package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.Database

import javax.inject.Inject

import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.QuestStatisticsTable.NAME
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores how many quests of which quest types the user solved */
@Singleton class QuestStatisticsDao @Inject constructor(private val db: Database) {

    interface Listener {
        fun onAddedOne(questType: String)
        fun onSubtractedOne(questType: String)
        fun onReplacedAll()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getTotalAmount(): Int =
        db.queryOne(NAME, arrayOf("total($SUCCEEDED) as count")) { it.getInt("count") } ?: 0

    fun getAll(): Map<String, Int> =
        db.query(NAME) { it.getString(QUEST_TYPE) to it.getInt(SUCCEEDED) }.toMap()

    fun clear() {
        db.delete(NAME)
        listeners.forEach { it.onReplacedAll() }
    }

    fun replaceAll(amounts: Map<String, Int>) {
        db.transaction {
            db.delete(NAME)
            if (amounts.isNotEmpty()) {
                db.replaceMany(NAME,
                    arrayOf(QUEST_TYPE, SUCCEEDED),
                    amounts.map { arrayOf(it.key, it.value) }
                )
            }
        }
        listeners.forEach { it.onReplacedAll() }
    }

    fun addOne(questType: String) {
        db.transaction {
            // first ensure the row exists
            db.insertOrIgnore(NAME, listOf(
                QUEST_TYPE to questType,
                SUCCEEDED to 0
            ))

            // then increase by one
            db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $QUEST_TYPE = ?", arrayOf(questType))
        }

        listeners.forEach { it.onAddedOne(questType) }
    }

    fun subtractOne(questType: String) {
        db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $QUEST_TYPE = ?", arrayOf(questType))
        listeners.forEach { it.onSubtractedOne(questType) }
    }

    fun getAmount(questType: String): Int =
        db.queryOne(NAME,
            columns = arrayOf(SUCCEEDED),
            where = "$QUEST_TYPE = ?",
            args = arrayOf(questType)
        ) { it.getInt(SUCCEEDED) } ?: 0

    fun getAmount(questTypes: List<String>): Int {
        val questionMarks = Array(questTypes.size) { "?" }.joinToString(",")
        return db.queryOne(NAME,
            columns = arrayOf("total($SUCCEEDED) as count"),
            where = "$QUEST_TYPE in ($questionMarks)",
            args = questTypes.toTypedArray()
        ) { it.getInt("count") } ?: 0
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}
