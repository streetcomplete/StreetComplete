package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ACTION
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.CREATED_TIMESTAMP
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.IS_SYNCED
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.TYPE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.NAME
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import javax.inject.Inject

class ElementEditsDao @Inject constructor(
    private val db: Database,
    private val questTypeRegistry: QuestTypeRegistry,
    private val serializer: Serializer
) {
    fun add(edit: ElementEdit) {
        val rowId = db.insert(NAME, edit.toPairs())
        edit.id = rowId
    }

    fun get(id: Long): ElementEdit? =
        db.queryOne(NAME, where = "$ID = $id") { it.toElementEdit() }

    fun getByElement(elementType: Element.Type, elementId: Long): List<ElementEdit> =
        db.query(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(elementType.name, elementId),
            orderBy = "$IS_SYNCED, $CREATED_TIMESTAMP"
        ) { it.toElementEdit() }

    fun getOldestUnsynced(): ElementEdit? =
        db.queryOne(NAME,
            where = "$IS_SYNCED = 0",
            orderBy = CREATED_TIMESTAMP
        ) { it.toElementEdit() }

    fun getUnsyncedCount(): Int =
        db.queryOne(NAME,
            columns = arrayOf("COUNT(*) AS count"),
            where = "$IS_SYNCED = 0"
        ) { it.getInt("count") } ?: 0

    fun getAllUnsynced(): List<ElementEdit> =
        db.query(NAME, where = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { it.toElementEdit() }

    fun getAll(): List<ElementEdit> =
        db.query(NAME, orderBy = "$IS_SYNCED, $CREATED_TIMESTAMP") { it.toElementEdit() }

    fun markSynced(id: Long): Boolean =
        db.update(NAME, listOf(IS_SYNCED to 1), "$ID = $id") == 1

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$ID = $id") == 1

    fun deleteAll(ids: List<Long>): Int =
        db.delete(NAME, "$ID in (${ids.joinToString(",")})")

    fun getSyncedOlderThan(timestamp: Long): List<ElementEdit> =
        db.query(NAME, where = "$IS_SYNCED = 1 AND $CREATED_TIMESTAMP < $timestamp") { it.toElementEdit() }

    fun updateElementId(elementType: Element.Type, oldElementId: Long, newElementId: Long): Int =
        db.update(
            NAME,
            values = listOf(ELEMENT_ID to newElementId),
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(elementType.name, oldElementId)
        )

    private fun ElementEdit.toPairs(): List<Pair<String, Any?>> = listOf(
        QUEST_TYPE to questType.name,
        ELEMENT_TYPE to elementType.name,
        ELEMENT_ID to elementId,
        SOURCE to source,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        CREATED_TIMESTAMP to createdTimestamp,
        IS_SYNCED to if (isSynced) 1 else 0,
        TYPE to action::class.simpleName,
        ACTION to when(action) {
            is UpdateElementTagsAction       -> serializer.toBytes(action.createSerializable())
            is RevertUpdateElementTagsAction -> serializer.toBytes(action)
            is DeletePoiNodeAction           -> serializer.toBytes(action)
            is SplitWayAction                -> serializer.toBytes(action)
            else -> null
        }
    )

    private fun CursorPosition.toElementEdit(): ElementEdit {
        val b = getBlobOrNull(ACTION)
        val type = getString(TYPE)

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
            getLong(ID),
            questTypeRegistry.getByName(getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            Element.Type.valueOf(getString(ELEMENT_TYPE)),
            getLong(ELEMENT_ID),
            getString(SOURCE),
            OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
            getLong(CREATED_TIMESTAMP),
            getInt(IS_SYNCED) == 1,
            action
        )
    }
}

private val OsmElementQuestType<*>.name get() = this::class.simpleName
