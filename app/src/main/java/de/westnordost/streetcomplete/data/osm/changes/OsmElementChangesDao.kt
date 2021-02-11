package de.westnordost.streetcomplete.data.osm.changes

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.CHANGES
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.CREATED_TIMESTAMP
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.IS_SYNCED
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.Columns.TYPE
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable.NAME
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import javax.inject.Inject

class OsmElementChangesDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmElementChangesMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(change: OsmElementChange) {
        val rowId = db.insertOrThrow(NAME, null, mapping.toContentValues(change))
        change.id = rowId
    }

    fun get(id: Long): OsmElementChange? =
        db.queryOne(NAME, selection = "$ID = $id") { mapping.toObject(it) }

    fun getOldestUnsynced(): OsmElementChange? =
        db.queryOne(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getUnsyncedCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*)"), "$IS_SYNCED = 0") { it.getInt(0) } ?: 0

    fun getAllUnsynced(): List<OsmElementChange> =
        db.query(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getAll(): List<OsmElementChange> =
        db.query(NAME, orderBy = "$IS_SYNCED, $CREATED_TIMESTAMP") { mapping.toObject(it) }

    fun markSynced(id: Long): Boolean =
        db.update(NAME, contentValuesOf(IS_SYNCED to 1), "$ID = $id", null) == 1

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$ID = $id", null) == 1

    fun deleteSyncedOlderThan(timestamp: Long): Int =
        db.delete(NAME, "$IS_SYNCED = 1 AND $CREATED_TIMESTAMP < $timestamp", null)

    fun updateElementId(elementType: Element.Type, oldElementId: Long, newElementId: Long): Int =
        db.update(
            NAME,
            contentValuesOf(ELEMENT_ID to newElementId),
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            arrayOf(elementType.name, oldElementId.toString())
        )

    fun deleteAllForElement(elementType: Element.Type, elementId: Long): Int =
        db.delete(
            NAME,
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            arrayOf(elementType.name, elementId.toString())
        )
}

class OsmElementChangesMapping @Inject constructor(
    private val questTypeRegistry: QuestTypeRegistry,
    private val serializer: Serializer
) : ObjectRelationalMapping<OsmElementChange> {

    override fun toContentValues(obj: OsmElementChange): ContentValues {
        val values = contentValuesOf(
            ID to obj.id,
            QUEST_TYPE to obj.questType.name,
            ELEMENT_TYPE to obj.elementType.name,
            ELEMENT_ID to obj.elementId,
            SOURCE to obj.source,
            LATITUDE to obj.position.latitude,
            LONGITUDE to obj.position.longitude,
            CREATED_TIMESTAMP to obj.createdTimestamp,
            IS_SYNCED to if (obj.isSynced) 1 else 0,
            TYPE to obj::class.simpleName
        )
        when(obj) {
            is ChangeOsmElementTags       -> values.put(CHANGES, serializer.toBytes(obj.changes))
            is RevertChangeOsmElementTags -> values.put(CHANGES, serializer.toBytes(obj.changes))
            is DeleteOsmElement           -> values.putNull(CHANGES)
            is SplitOsmWay                -> values.put(CHANGES, serializer.toBytes(obj.splits))
        }

        return values
    }

    override fun toObject(cursor: Cursor): OsmElementChange {
        val id = cursor.getLong(ID)
        val questType = questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>
        val elementType = Element.Type.valueOf(cursor.getString(ELEMENT_TYPE))
        val elementId = cursor.getLong(ELEMENT_ID)
        val source =cursor.getString(SOURCE)
        val pos = OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE))
        val created = cursor.getLong(CREATED_TIMESTAMP)
        val synced = cursor.getInt(IS_SYNCED) == 1
        val changes = cursor.getBlobOrNull(CHANGES)
        val type = cursor.getString(TYPE)

        return when(type) {
            ChangeOsmElementTags::class.simpleName -> ChangeOsmElementTags(
                id, questType, elementType, elementId, source, pos, created, synced, serializer.toObject(changes!!)
            )
            RevertChangeOsmElementTags::class.simpleName -> RevertChangeOsmElementTags(
                id, questType, elementType, elementId, source, pos, created, synced, serializer.toObject(changes!!)
            )
            DeleteOsmElement::class.simpleName -> DeleteOsmElement(
                id, questType, elementType, elementId, source, pos, created, synced
            )
            SplitOsmWay::class.simpleName -> SplitOsmWay(
                id, questType, elementType, elementId, source, pos, created, synced, serializer.toObject(changes!!)
            )
            else -> throw IllegalStateException("Unknown change class $type")
        }
    }
}

private val OsmElementQuestType<*>.name get() = this::class.simpleName
