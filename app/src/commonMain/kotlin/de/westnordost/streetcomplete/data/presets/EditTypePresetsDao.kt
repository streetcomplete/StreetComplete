package de.westnordost.streetcomplete.data.presets

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.presets.EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID
import de.westnordost.streetcomplete.data.presets.EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_NAME
import de.westnordost.streetcomplete.data.presets.EditTypePresetsTable.NAME

/** Stores the ids and names of edit type presets */
class EditTypePresetsDao(private val db: Database) {

    fun add(name: String): Long =
        db.insert(NAME, listOf(EDIT_TYPE_PRESET_NAME to name))

    fun rename(id: Long, name: String) {
        db.update(NAME,
            values = listOf(EDIT_TYPE_PRESET_NAME to name),
            where = "$EDIT_TYPE_PRESET_ID = $id"
        )
    }

    fun delete(id: Long) {
        db.delete(NAME, "$EDIT_TYPE_PRESET_ID = $id")
    }

    fun getAll(): List<EditTypePreset> =
        db.query(NAME, orderBy = "$EDIT_TYPE_PRESET_ID ASC") {
            EditTypePreset(it.getLong(EDIT_TYPE_PRESET_ID), it.getString(EDIT_TYPE_PRESET_NAME))
        }

    fun getName(presetId: Long): String? =
        db.queryOne(NAME, where = "$EDIT_TYPE_PRESET_ID = $presetId") { it.getString(EDIT_TYPE_PRESET_NAME) }
}

data class EditTypePreset(val id: Long, val name: String)
