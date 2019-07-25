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

/** Undos of OsmQuests  */
class UndoOsmQuestDao @Inject constructor(
    private var dbHelper: SQLiteOpenHelper,
    private var serializer: Serializer,
    private var questTypeList: QuestTypeRegistry
) {
    fun getAll(): List<UndoOsmQuest> {
	    return dbHelper.readableDatabase.query(NAME_MERGED_VIEW).use { cursor ->
		    return cursor.map { it.createUndo() }
	    }
    }

    fun delete(questId: Long): Int {
        return dbHelper.writableDatabase.delete(NAME, "$QUEST_ID = $questId", null)
    }

    fun add(quest: UndoOsmQuest) {
        dbHelper.writableDatabase.insert(NAME, null, quest.createContentValues())
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
