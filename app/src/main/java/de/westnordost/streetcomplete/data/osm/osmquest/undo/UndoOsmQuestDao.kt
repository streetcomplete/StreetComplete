package de.westnordost.streetcomplete.data.osm.osmquest.undo

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping

import javax.inject.Inject

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryMapping
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores UndoOsmQuest objects - to reverse a previously already uploaded change through OsmQuest */
@Singleton class UndoOsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: UndoOsmQuestMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedUndoOsmQuest()
        fun onDeletedUndoOsmQuest()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getAll(): List<UndoOsmQuest> {
        return db.query(NAME_MERGED_VIEW) { mapping.toObject(it) }
    }

    fun get(questId: Long): UndoOsmQuest? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(questId.toString())
        return db.queryOne(NAME_MERGED_VIEW, null, selection, args) { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun delete(questId: Long): Boolean {
        val result = db.delete(NAME, "$QUEST_ID = ?", arrayOf(questId.toString())) == 1
        if (result) listeners.forEach { it.onDeletedUndoOsmQuest() }
        return result
    }

    fun add(quest: UndoOsmQuest) {
        db.insertOrThrow(NAME, null, mapping.toContentValues(quest))
        listeners.forEach { it.onAddedUndoOsmQuest() }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
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
