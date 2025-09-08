package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable.Columns.EDIT_TYPE
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable.Columns.VISIBILITY
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable.NAME

/** Stores which edit types are visible by user selection and which are not */
class VisibleEditTypeDao(private val db: Database) {

    fun put(presetId: Long, editTypeName: String, visible: Boolean) {
        db.replace(NAME, listOf(
            EDIT_TYPE_PRESET_ID to presetId,
            EDIT_TYPE to editTypeName,
            VISIBILITY to if (visible) 1 else 0
        ))
    }

    fun putAll(presetId: Long, editTypeVisibilities: Map<String, Boolean>) {
        db.replaceMany(NAME,
            arrayOf(EDIT_TYPE_PRESET_ID, EDIT_TYPE, VISIBILITY),
            editTypeVisibilities.map { (editTypeName, visibility) ->
                arrayOf(presetId, editTypeName, if (visibility) 1 else 0)
            }
        )
    }

    fun get(presetId: Long, editTypeName: String): Boolean =
        db.queryOne(NAME,
            columns = arrayOf(VISIBILITY),
            where = "$EDIT_TYPE_PRESET_ID = ? AND $EDIT_TYPE = ?",
            args = arrayOf(presetId, editTypeName)
        ) { it.getInt(VISIBILITY) != 0 } ?: true

    fun getAll(presetId: Long): MutableMap<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        db.query(NAME, where = "$EDIT_TYPE_PRESET_ID = $presetId") { cursor ->
            val editTypeName = cursor.getString(EDIT_TYPE)
            val visible = cursor.getInt(VISIBILITY) != 0
            result[editTypeName] = visible
        }
        return result
    }

    fun clear(presetId: Long, editTypeNames: Collection<String>) {
        val questionMarks = Array(editTypeNames.size) { "?" }.joinToString(",")
        db.delete(NAME,
            where = "$EDIT_TYPE_PRESET_ID = $presetId AND $EDIT_TYPE IN ($questionMarks)",
            args = editTypeNames.toTypedArray()
        )
    }
}
