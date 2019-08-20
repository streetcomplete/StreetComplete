package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.osmapi.map.data.Element

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer

class UndoOsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry
) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): List<UndoOsmQuest> {
        return db.query(NAME_MERGED_VIEW) { it.createUndo() }
    }

    fun get(questId: Long): UndoOsmQuest? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(questId.toString())
        return db.queryOne(NAME_MERGED_VIEW, null, selection, args) { it.createUndo() }
    }

    fun delete(questId: Long) {
        db.delete(NAME, "$QUEST_ID = $questId", null)
    }

    fun add(quest: UndoOsmQuest) {
        db.insert(NAME, null, quest.createContentValues())
    }

    private fun UndoOsmQuest.createContentValues() = ContentValues().also { v ->
        v.put(QUEST_ID, id)
        v.put(QUEST_TYPE, type.javaClass.simpleName)
        v.put(TAG_CHANGES, serializer.toBytes(changes))
        v.put(CHANGES_SOURCE, changesSource)
        v.put(ELEMENT_TYPE, elementType.name)
        v.put(ELEMENT_ID, elementId)
    }

    private fun Cursor.createUndo() = UndoOsmQuest(
        getLong(QUEST_ID),
        questTypeList.getByName(getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        Element.Type.valueOf(getString(ELEMENT_TYPE)),
        getLong(ELEMENT_ID),
        serializer.toObject(getBlob(TAG_CHANGES)),
        getString(CHANGES_SOURCE),
        ElementGeometryDao.createObjectFrom(serializer, this)
    )
}
