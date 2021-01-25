package de.westnordost.streetcomplete.data.osm.osmquest

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import java.util.Date

import javax.inject.Inject
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.*
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.QUEST_STATUS
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.data.quest.QuestStatus.*
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryMapping
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer

/** Stores OsmQuest objects - quests and answers to these for adding data to OSM.
 *
 *  This is just a simple CRUD-like DAO, use OsmQuestController to access the data from the
 *  application logic.
 *  */
internal class OsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmQuestMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(quest: OsmQuest): Boolean {
        quest.lastUpdate = Date()
        val rowId = db.insertWithOnConflict(NAME, null, mapping.toContentValues(quest), CONFLICT_IGNORE)
        if (rowId == -1L) return false
        quest.id = rowId
        return true
    }

    fun get(id: Long): OsmQuest? {
        return db.queryOne(NAME_MERGED_VIEW, null, "$QUEST_ID = $id") { mapping.toObject(it) }
    }

    fun getLastSolved(): OsmQuest? {
        val qb = createQuery(statusIn = listOf(HIDDEN, ANSWERED, CLOSED))
        return db.queryOne(NAME_MERGED_VIEW, null, qb, "$LAST_UPDATE DESC") { mapping.toObject(it) }
    }

    fun getCount(
        statusIn: Collection<QuestStatus>? = null,
        bounds: BoundingBox? = null,
        element: ElementKey? = null,
        questTypes: Collection<String>? = null,
        changedBefore: Long? = null
    ): Int {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        return db.queryOne(NAME_MERGED_VIEW, arrayOf("COUNT(*)"), qb) { it.getInt(0) } ?: 0
    }

    fun update(quest: OsmQuest): Boolean {
        quest.lastUpdate = Date()
        return db.update(NAME, mapping.toUpdatableContentValues(quest), "$QUEST_ID = ${quest.id}", null) == 1
    }

    fun delete(id: Long): Boolean {
        return db.delete(NAME, "$QUEST_ID = $id", null) == 1
    }

    fun addAll(quests: Collection<OsmQuest>): Int {
        if (quests.isEmpty()) return 0
        var addedRows = 0
        db.transaction {
            for (quest in quests) {
                if (add(quest)) addedRows++
            }
        }
        return addedRows
    }

    fun getAll(
        statusIn: Collection<QuestStatus>? = null,
        bounds: BoundingBox? = null,
        element: ElementKey? = null,
        questTypes: Collection<String>? = null,
        changedBefore: Long? = null
    ): List<OsmQuest> {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        return db.query(NAME_MERGED_VIEW, null, qb) { mapping.toObject(it) }.filterNotNull()
    }

    fun getAllIds(
        statusIn: Collection<QuestStatus>? = null,
        bounds: BoundingBox? = null,
        element: ElementKey? = null,
        questTypes: Collection<String>? = null,
        changedBefore: Long? = null
    ): List<Long> {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        return db.query(NAME_MERGED_VIEW, arrayOf(QUEST_ID), qb) { it.getLong(0) }
    }

    fun updateAll(quests: Collection<OsmQuest>): Int {
        if (quests.isEmpty()) return 0
        var rows = 0
        db.transaction {
            quests.forEach {
                if (update(it)) rows++
            }
        }
        return rows
    }

    fun deleteAllIds(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
    }

    fun deleteAll(
        statusIn: Collection<QuestStatus>? = null,
        bounds: BoundingBox? = null,
        element: ElementKey? = null,
        questTypes: Collection<String>? = null,
        changedBefore: Long? = null
    ): Int {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        return db.delete(NAME, qb.where, qb.args)
    }
}

private fun createQuery(
        statusIn: Collection<QuestStatus>? = null,
        bounds: BoundingBox? = null,
        element: ElementKey? = null,
        questTypes: Collection<String>? = null,
        changedBefore: Long? = null
) = WhereSelectionBuilder().apply {
    if (element != null) {
        add("$ELEMENT_TYPE = ?", element.elementType.name)
        add("$ELEMENT_ID = ?", element.elementId.toString())
    }
    if (statusIn != null) {
        require(statusIn.isNotEmpty()) { "statusIn must not be empty if not null" }
        if (statusIn.size == 1) {
            add("$QUEST_STATUS = ?", statusIn.single().name)
        } else {
            val names = statusIn.joinToString(",") { "\"$it\"" }
            add("$QUEST_STATUS IN ($names)")
        }
    }
    if (questTypes != null) {
        require(questTypes.isNotEmpty()) { "questTypes must not be empty if not null" }
        if (questTypes.size == 1) {
            add("$QUEST_TYPE = ?", questTypes.single())
        } else {
            val names = questTypes.joinToString(",") { "\"$it\"" }
            add("$QUEST_TYPE IN ($names)")
        }
    }
    if (bounds != null) {
        add(
            "(${ElementGeometryTable.Columns.CENTER_LATITUDE} BETWEEN ? AND ?)",
            bounds.minLatitude.toString(),
            bounds.maxLatitude.toString()
        )
        add(
            "(${ElementGeometryTable.Columns.CENTER_LONGITUDE} BETWEEN ? AND ?)",
            bounds.minLongitude.toString(),
            bounds.maxLongitude.toString()
        )
    }
    if (changedBefore != null) {
        add("$LAST_UPDATE < ?", changedBefore.toString())
    }
}

class OsmQuestMapping @Inject constructor(
        private val serializer: Serializer,
        private val questTypeRegistry: QuestTypeRegistry,
        private val elementGeometryMapping: ElementGeometryMapping
) : ObjectRelationalMapping<OsmQuest?> {

    override fun toContentValues(obj: OsmQuest?) =
        obj?.let { toConstantContentValues(it) + toUpdatableContentValues(it) } ?: contentValuesOf()

    override fun toObject(cursor: Cursor): OsmQuest? {
        val questType = questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) ?: return null
        return OsmQuest(
            cursor.getLong(QUEST_ID),
            questType as OsmElementQuestType<*>,
            Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
            cursor.getLong(ELEMENT_ID),
            valueOf(cursor.getString(QUEST_STATUS)),
            cursor.getBlobOrNull(TAG_CHANGES)?.let { serializer.toObject<StringMapChanges>(it) },
            cursor.getStringOrNull(CHANGES_SOURCE),
            Date(cursor.getLong(LAST_UPDATE)),
            elementGeometryMapping.toObject(cursor)
        )
    }

    fun toUpdatableContentValues(obj: OsmQuest) = contentValuesOf(
        QUEST_STATUS to obj.status.name,
        TAG_CHANGES to obj.changes?.let { serializer.toBytes(it) },
        CHANGES_SOURCE to obj.changesSource,
        LAST_UPDATE to obj.lastUpdate.time
    )

    private fun toConstantContentValues(obj: OsmQuest) = contentValuesOf(
        QUEST_ID to obj.id,
        QUEST_TYPE to obj.type.javaClass.simpleName,
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId
    )
}
