package de.westnordost.streetcomplete.data.statistics

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.common.Handler

import javax.inject.Inject

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsTable.NAME
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.transaction

class QuestStatisticsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val userChangesetsDao: UserChangesetsDao
) {
    private val db get() = dbHelper.writableDatabase

    fun getNoteAmount(): Int {
        return getAmount(NOTE)
    }

    fun getTotalAmount(): Int {
        return db.queryOne(NAME, arrayOf("total($SUCCEEDED)")) { it.getInt(0) } ?: 0
    }

    fun syncFromOsmServer(userId: Long) {
        val data = HashMap<String, Int>()

        userChangesetsDao.findAll(Handler { changeset ->
            if(changeset?.tags?.get("created_by")?.startsWith(ApplicationConstants.NAME) == true) {
                val questType = changeset.tags?.get(ApplicationConstants.QUESTTYPE_TAG_KEY)
                if (questType != null) {
                    val prev = data[questType] ?: 0
                    data[questType] = prev + changeset.changesCount
                }
            }
        }, userId, ApplicationConstants.DATE_OF_BIRTH)

        db.transaction {
            // clear table
            db.delete(NAME, null, null)
            for ((key, value) in data) {
                db.insert(NAME, null, contentValuesOf(
                    QUEST_TYPE to key,
                    SUCCEEDED to value
                ))
            }
        }
    }

    fun addOneNote() {
        addOne(NOTE)
    }

    fun addOne(questType: String) {
        // first ensure the row exists
        db.insertWithOnConflict(NAME, null, contentValuesOf(
            QUEST_TYPE to questType,
            SUCCEEDED to 0
        ), CONFLICT_IGNORE)

        // then increase by one
        db.execSQL("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $QUEST_TYPE = ?",
            arrayOf(questType))
    }

    fun getAmount(questType: String): Int {
        return db.queryOne(NAME, arrayOf(SUCCEEDED), "$QUEST_TYPE = ?", arrayOf(questType)) {
            it.getInt(0)
        } ?: 0
    }
}

private const val NOTE = "NOTE"
