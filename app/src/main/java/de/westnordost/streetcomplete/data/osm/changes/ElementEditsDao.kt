package de.westnordost.streetcomplete.data.osm.changes

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.ACTION
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.CREATED_TIMESTAMP
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.IS_SYNCED
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.Columns.TYPE
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable.NAME
import de.westnordost.streetcomplete.data.osm.changes.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.changes.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.changes.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.changes.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import javax.inject.Inject

class ElementEditsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: ElementEditsMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(edit: ElementEdit) {
        val rowId = db.insertOrThrow(NAME, null, mapping.toContentValues(edit))
        edit.id = rowId
    }

    fun get(id: Long): ElementEdit? =
        db.queryOne(NAME, selection = "$ID = $id") { mapping.toObject(it) }

    fun getOldestUnsynced(): ElementEdit? =
        db.queryOne(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getUnsyncedCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*)"), "$IS_SYNCED = 0") { it.getInt(0) } ?: 0

    fun getAllUnsynced(): List<ElementEdit> =
        db.query(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getAll(): List<ElementEdit> =
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

class ElementEditsMapping @Inject constructor(
    private val questTypeRegistry: QuestTypeRegistry,
    private val serializer: Serializer
) : ObjectRelationalMapping<ElementEdit> {

    override fun toContentValues(obj: ElementEdit): ContentValues {
        val action = obj.action
        val values = contentValuesOf(
            QUEST_TYPE to obj.questType.name,
            ELEMENT_TYPE to obj.elementType.name,
            ELEMENT_ID to obj.elementId,
            SOURCE to obj.source,
            LATITUDE to obj.position.latitude,
            LONGITUDE to obj.position.longitude,
            CREATED_TIMESTAMP to obj.createdTimestamp,
            IS_SYNCED to if (obj.isSynced) 1 else 0,
            TYPE to action::class.simpleName
        )
        when(action) {
            is UpdateElementTagsAction       -> values.put(ACTION, serializer.toBytes(action.createSerializable()))

            is RevertUpdateElementTagsAction -> values.put(ACTION, serializer.toBytes(action))

            is DeletePoiNodeAction           -> values.put(ACTION, serializer.toBytes(action))

            is SplitWayAction                -> values.put(ACTION, serializer.toBytes(action))
        }

        return values
    }

    override fun toObject(cursor: Cursor): ElementEdit {
        val b = cursor.getBlobOrNull(ACTION)
        val type = cursor.getString(TYPE)

        val action = when(type) {
            UpdateElementTagsAction::class.simpleName ->
                serializer.toObject<UpdateElementTagsAction.Serializable>(b!!).createObject(questTypeRegistry)

            RevertUpdateElementTagsAction::class.simpleName ->
                serializer.toObject<RevertUpdateElementTagsAction>(b!!)

            DeletePoiNodeAction::class.simpleName ->
                serializer.toObject<DeletePoiNodeAction>(b!!)

            SplitWayAction::class.simpleName ->
                serializer.toObject<SplitWayAction>(b!!)

            else -> throw IllegalStateException("Unknown change class $type")
        }

        return ElementEdit(
            cursor.getLong(ID),
            questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
            cursor.getLong(ELEMENT_ID),
            cursor.getString(SOURCE),
            OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
            cursor.getLong(CREATED_TIMESTAMP),
            cursor.getInt(IS_SYNCED) == 1,
            action
        )
    }
}

private val OsmElementQuestType<*>.name get() = this::class.simpleName
