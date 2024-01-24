package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable.Columns.QUEST_PRESET_ID
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable.Columns.QUEST_PRESET_NAME
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable.NAME

/** Stores the ids and names of quest presets */
class QuestPresetsDao(private val db: Database) {

    fun add(name: String): Long =
        db.insert(NAME, listOf(QUEST_PRESET_NAME to name))

    fun rename(id: Long, name: String) {
        db.update(NAME,
            values = listOf(QUEST_PRESET_NAME to name),
            where = "$QUEST_PRESET_ID = $id"
        )
    }

    fun delete(id: Long) {
        db.delete(NAME, "$QUEST_PRESET_ID = $id")
    }

    fun getAll(): List<QuestPreset> =
        db.query(NAME, orderBy = "$QUEST_PRESET_ID ASC") {
            QuestPreset(it.getLong(QUEST_PRESET_ID), it.getString(QUEST_PRESET_NAME))
        }

    fun getName(presetId: Long): String? =
        db.queryOne(NAME, where = "$QUEST_PRESET_ID = $presetId") { it.getString(QUEST_PRESET_NAME) }
}

data class QuestPreset(val id: Long, val name: String)
