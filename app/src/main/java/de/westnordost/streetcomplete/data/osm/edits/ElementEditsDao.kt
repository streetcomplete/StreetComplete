package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ACTION
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.CREATED_TIMESTAMP
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.GEOMETRY
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.IS_SYNCED
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable.NAME
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.RevertCreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.delete.RevertDeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.MoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.move.RevertMoveNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.RevertUpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeFromVertexAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class ElementEditsDao(
    private val db: Database,
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry
) {
    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(ElementEditAction::class) {
                subclass(UpdateElementTagsAction::class)
                subclass(RevertUpdateElementTagsAction::class)
                subclass(SplitWayAction::class)
                subclass(DeletePoiNodeAction::class)
                subclass(RevertDeletePoiNodeAction::class)
                subclass(CreateNodeAction::class)
                subclass(RevertCreateNodeAction::class)
                subclass(MoveNodeAction::class)
                subclass(RevertMoveNodeAction::class)
                subclass(CreateNodeFromVertexAction::class)
            }
        }
    }

    fun put(edit: ElementEdit) {
        val rowId = db.insert(NAME, edit.toPairs())
        edit.id = rowId
    }

    fun get(id: Long): ElementEdit? =
        db.queryOne(NAME, where = "$ID = $id") { it.toElementEdit() }

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

    fun deleteAll(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$ID in (${ids.joinToString(",")})")
    }

    fun getSyncedOlderThan(timestamp: Long): List<ElementEdit> =
        db.query(NAME, where = "$IS_SYNCED = 1 AND $CREATED_TIMESTAMP < $timestamp") { it.toElementEdit() }

    private fun ElementEdit.toPairs(): List<Pair<String, Any?>> = listOf(
        QUEST_TYPE to type.name,
        GEOMETRY to json.encodeToString(originalGeometry),
        SOURCE to source,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        CREATED_TIMESTAMP to createdTimestamp,
        IS_SYNCED to if (isSynced) 1 else 0,
        ACTION to json.encodeToString(action)
    )

    private fun CursorPosition.toElementEdit() = ElementEdit(
        getLong(ID),
        questTypeRegistry.getByName(getString(QUEST_TYPE)) as? OsmElementQuestType<*>
            ?: overlayRegistry.getByName(getString(QUEST_TYPE))!!,
        json.decodeFromString(getString(GEOMETRY)),
        getString(SOURCE),
        getLong(CREATED_TIMESTAMP),
        getInt(IS_SYNCED) == 1,
        json.decodeFromString(getString(ACTION))
    )
}
