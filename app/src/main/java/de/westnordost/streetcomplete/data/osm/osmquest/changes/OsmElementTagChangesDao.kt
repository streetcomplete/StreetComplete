package de.westnordost.streetcomplete.data.osm.osmquest.changes

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping

import javax.inject.Inject

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.IS_REVERT
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesTable.NAME
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores ElementTagChanges objects */
@Singleton class OsmElementTagChangesDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: ElementTagChangesMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedElementTagChanges()
        fun onDeletedElementTagChanges()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getAll(): List<OsmElementTagChanges> {
        return db.query(NAME, orderBy = "$ID ASC") { mapping.toObject(it) }
    }

    fun get(id: Long): OsmElementTagChanges? {
        val selection = "$ID = ?"
        val args = arrayOf(id.toString())
        return db.queryOne(NAME, null, selection, args) { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun delete(id: Long): Boolean {
        val result = db.delete(NAME, "$ID = ?", arrayOf(id.toString())) == 1
        if (result) listeners.forEach { it.onDeletedElementTagChanges() }
        return result
    }

    fun add(changes: OsmElementTagChanges) {
        db.insertOrThrow(NAME, null, mapping.toContentValues(changes))
        listeners.forEach { it.onAddedElementTagChanges() }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

class ElementTagChangesMapping @Inject constructor(
        private val serializer: Serializer,
        private val questTypeList: QuestTypeRegistry
) : ObjectRelationalMapping<OsmElementTagChanges> {

    override fun toContentValues(obj: OsmElementTagChanges) = contentValuesOf(
        ID to obj.id,
        QUEST_TYPE to obj.osmElementQuestType.javaClass.simpleName,
        TAG_CHANGES to serializer.toBytes(obj.changes),
        CHANGES_SOURCE to obj.source,
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId,
        IS_REVERT to if (obj.isRevert) 1 else 0
    )

    override fun toObject(cursor: Cursor) = OsmElementTagChanges(
        cursor.getLong(ID),
        questTypeList.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
        cursor.getLong(ELEMENT_ID),
        serializer.toObject(cursor.getBlob(TAG_CHANGES)),
        cursor.getString(CHANGES_SOURCE),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
        cursor.getInt(IS_REVERT) == 1
    )
}
