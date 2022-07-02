package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTable.Columns.ELEMENT_EDIT_TYPE
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTable.Columns.SUCCEEDED
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTable.NAME

/** Stores how many edits of which element type the user did */
class EditTypeStatisticsDao(private val db: Database) {

    fun getTotalAmount(): Int =
        db.queryOne(NAME, arrayOf("total($SUCCEEDED) as count")) { it.getInt("count") } ?: 0

    fun getAll(): List<EditTypeStatistics> =
        db.query(NAME) { it.toEditTypeStatistics() }

    fun clear() {
        db.delete(NAME)
    }

    fun replaceAll(amounts: Map<String, Int>) {
        db.transaction {
            db.delete(NAME)
            if (amounts.isNotEmpty()) {
                db.replaceMany(NAME,
                    arrayOf(ELEMENT_EDIT_TYPE, SUCCEEDED),
                    amounts.map { arrayOf(it.key, it.value) }
                )
            }
        }
    }

    fun addOne(type: String) {
        db.transaction {
            // first ensure the row exists
            db.insertOrIgnore(NAME, listOf(
                ELEMENT_EDIT_TYPE to type,
                SUCCEEDED to 0
            ))

            // then increase by one
            db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED + 1 WHERE $ELEMENT_EDIT_TYPE = ?", arrayOf(type))
        }
    }

    fun subtractOne(type: String) {
        db.exec("UPDATE $NAME SET $SUCCEEDED = $SUCCEEDED - 1 WHERE $ELEMENT_EDIT_TYPE = ?", arrayOf(type))
    }

    fun getAmount(type: String): Int =
        db.queryOne(NAME,
            columns = arrayOf(SUCCEEDED),
            where = "$ELEMENT_EDIT_TYPE = ?",
            args = arrayOf(type)
        ) { it.getInt(SUCCEEDED) } ?: 0

    fun getAmount(type: List<String>): Int {
        val questionMarks = Array(type.size) { "?" }.joinToString(",")
        return db.queryOne(NAME,
            columns = arrayOf("total($SUCCEEDED) as count"),
            where = "$ELEMENT_EDIT_TYPE in ($questionMarks)",
            args = type.toTypedArray()
        ) { it.getInt("count") } ?: 0
    }
}

private fun CursorPosition.toEditTypeStatistics() = EditTypeStatistics(
    getString(ELEMENT_EDIT_TYPE),
    getInt(SUCCEEDED)
)
