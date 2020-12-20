package de.westnordost.streetcomplete.data.osm.delete_element

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementTable.NAME
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Stores OsmQuestSplitWay objects by quest ID - the solutions of "differs along the way" quest
 *  answers. */
@Singleton class DeleteOsmElementDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: DeleteOsmElementMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedDeleteOsmElement()
        fun onDeletedDeleteOsmElement()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getAll(): List<DeleteOsmElement> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun get(questId: Long): DeleteOsmElement? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(questId.toString())
        return db.queryOne(NAME, null, selection, args) { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun add(quest: DeleteOsmElement) {
        db.insertOrThrow(NAME, null, mapping.toContentValues(quest))
        listeners.forEach { it.onAddedDeleteOsmElement() }
    }

    fun delete(questId: Long): Boolean {
        val result = db.delete(NAME, "$QUEST_ID = ?", arrayOf(questId.toString())) == 1
        if (result) listeners.forEach { it.onDeletedDeleteOsmElement() }
        return result
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

class DeleteOsmElementMapping @Inject constructor(
    private val questTypeList: QuestTypeRegistry
) : ObjectRelationalMapping<DeleteOsmElement> {

    override fun toContentValues(obj: DeleteOsmElement) = contentValuesOf(
        QUEST_ID to obj.questId,
        QUEST_TYPE to obj.questType.javaClass.simpleName,
        ELEMENT_ID to obj.elementId,
        ELEMENT_TYPE to obj.elementType.name,
        SOURCE to obj.source,
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude
    )

    override fun toObject(cursor: Cursor) = DeleteOsmElement(
        cursor.getLong(QUEST_ID),
        questTypeList.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        cursor.getLong(ELEMENT_ID),
        Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
        cursor.getString(SOURCE),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE))
    )
}
