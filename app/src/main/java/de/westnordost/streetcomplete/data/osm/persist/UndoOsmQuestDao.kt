package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping

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
    private val mapping: UndoOsmQuestMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): List<UndoOsmQuest> {
        return db.query(NAME_MERGED_VIEW) { mapping.toObject(it) }
    }

    fun get(questId: Long): UndoOsmQuest? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(questId.toString())
        return db.queryOne(NAME_MERGED_VIEW, null, selection, args) { mapping.toObject(it) }
    }

    fun delete(questId: Long) {
        db.delete(NAME, "$QUEST_ID = $questId", null)
    }

    fun add(quest: UndoOsmQuest) {
        db.insert(NAME, null, mapping.toContentValues(quest))
    }
}

class UndoOsmQuestMapping @Inject constructor(
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry,
    private val elementGeometryMapping: ElementGeometryMapping
) : ObjectRelationalMapping<UndoOsmQuest> {

    override fun toContentValues(obj: UndoOsmQuest) = contentValuesOf(
        QUEST_ID to obj.id,
        QUEST_TYPE to obj.type.javaClass.simpleName,
        TAG_CHANGES to serializer.toBytes(obj.changes),
        CHANGES_SOURCE to obj.changesSource,
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId
    )

    override fun toObject(cursor: Cursor) = UndoOsmQuest(
        cursor.getLong(QUEST_ID),
        questTypeList.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
        cursor.getLong(ELEMENT_ID),
        serializer.toObject(cursor.getBlob(TAG_CHANGES)),
        cursor.getString(CHANGES_SOURCE),
        elementGeometryMapping.toObject(cursor)
    )
}
