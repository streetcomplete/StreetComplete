package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.osmapi.map.data.Element

import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.NAME_UNDO
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.NAME_UNDO_MERGED_VIEW
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.*

/** Undos of OsmQuests  */
class UndoOsmQuestDao @Inject constructor(
    private var dbHelper: SQLiteOpenHelper,
    private var serializer: Serializer,
    private var questTypeList: QuestTypeRegistry
) {
    fun getAll(): List<UndoOsmQuest> {
	    return dbHelper.readableDatabase.query(NAME_UNDO_MERGED_VIEW).use { cursor ->
		    val result = mutableListOf<UndoOsmQuest>()
		    while (cursor.moveToNext()) {
			    result.add(cursor.createUndo())
		    }
		    return result
	    }
    }

    fun delete(questId: Long): Int {
        return dbHelper.writableDatabase.delete(NAME_UNDO, "$QUEST_ID = $questId", null)
    }

    fun add(quest: UndoOsmQuest) {
        dbHelper.writableDatabase.insert(NAME_UNDO, null, quest.createContentValues())
    }

    private fun UndoOsmQuest.createContentValues() = ContentValues().also { v ->
        v.put(QUEST_ID, id)
        v.put(QUEST_TYPE, type.javaClass.simpleName)
        v.put(TAG_CHANGES, changes?.let { serializer.toBytes(it) })
        v.put(CHANGES_SOURCE, changesSource)
        v.put(ELEMENT_TYPE, elementType.name)
        v.put(ELEMENT_ID, elementId)
        v.put(LAST_UPDATE, Date().time)
    }

    private fun Cursor.createUndo() = UndoOsmQuest(
        getLong(QUEST_ID),
        questTypeList.getByName(getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        Element.Type.valueOf(getString(ELEMENT_TYPE)),
        getLong(ELEMENT_ID),
        getBlobOrNull(TAG_CHANGES)?.let { serializer.toObject(it) },
        getStringOrNull(CHANGES_SOURCE),
        Date(getLong(LAST_UPDATE)),
        ElementGeometryDao.createObjectFrom(serializer, this)
    )
}
