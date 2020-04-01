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
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores OsmQuest objects - quests and answers to these for adding data to OSM */
@Singleton class OsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmQuestMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    /** Listener on solved quests */
    interface AnsweredQuestCountListener {
        fun onAnsweredQuestCountIncreased()
        fun onAnsweredQuestCountDecreased()
    }

    private val answeredQuestCountListeners: MutableList<AnsweredQuestCountListener> = CopyOnWriteArrayList()

    var answeredCount: Int = getCount(statusIn = listOf(ANSWERED))
        private set

    fun add(quest: OsmQuest): Boolean {
        return addAll(listOf(quest)) == 1
    }

    fun get(id: Long): OsmQuest? {
        return db.queryOne(NAME_MERGED_VIEW, null, "$QUEST_ID = $id") { mapping.toObject(it) }
    }

    fun update(quest: OsmQuest): Boolean {
        quest.lastUpdate = Date()
        val result = db.update(NAME, mapping.toUpdatableContentValues(quest), "$QUEST_ID = ${quest.id}", null) == 1
        if (result) onUpdated()
        return result
    }

    fun delete(id: Long): Boolean {
        val result = db.delete(NAME, "$QUEST_ID = $id", null) == 1
        if (result) onUpdated()
        return result
    }

    fun addAll(quests: Collection<OsmQuest>): Int {
        var addedRows = 0
        db.transaction {
            for (quest in quests) {
                quest.lastUpdate = Date()
                val rowId = db.insertWithOnConflict(NAME, null, mapping.toContentValues(quest), CONFLICT_IGNORE)
                if (rowId != -1L) {
                    quest.id = rowId
                    addedRows++
                }
            }
        }
        if (addedRows > 0) onUpdated()
        return addedRows
    }

    fun deleteAllIds(ids: Collection<Long>): Int {
        val result = db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
        if (result > 0) onUpdated()
        return result
    }

    fun unhideAll(): Int {
        val values = contentValuesOf(QUEST_STATUS to NEW.name)
        val result = db.update(NAME, values, "$QUEST_STATUS = ?", arrayOf(HIDDEN.name))
        if (result > 0) onUpdated()
        return result
    }

    fun getLastSolved(): OsmQuest? {
        val qb = createQuery(statusIn = listOf(HIDDEN, ANSWERED, CLOSED))
        return db.queryOne(NAME_MERGED_VIEW, null, qb, "$LAST_UPDATE DESC") { mapping.toObject(it) }
    }

    fun getAll(
            statusIn: Collection<QuestStatus>? = null,
            bounds: BoundingBox? = null,
            element: ElementKey? = null,
            questTypes: Collection<String>? = null,
            changedBefore: Long? = null
    ): List<OsmQuest> {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        return db.query(NAME_MERGED_VIEW, null, qb) { mapping.toObject(it) }
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

    fun deleteAll(
            statusIn: Collection<QuestStatus>? = null,
            bounds: BoundingBox? = null,
            element: ElementKey? = null,
            questTypes: Collection<String>? = null,
            changedBefore: Long? = null
    ): Int {
        val qb = createQuery(statusIn, bounds, element, questTypes, changedBefore)
        val result = db.delete(NAME, qb.where, qb.args)
        if (result > 0) onUpdated()
        return result
    }

    fun addAnsweredQuestCountListener(listener: AnsweredQuestCountListener) {
        answeredQuestCountListeners.add(listener)
    }
    fun removeSolvedQuestCountListener(listener: AnsweredQuestCountListener) {
        answeredQuestCountListeners.remove(listener)
    }

    private fun onUpdated() {
        val newAnsweredQuestCount = getCount(statusIn = listOf(ANSWERED))
        if (newAnsweredQuestCount != answeredCount) {
            if (newAnsweredQuestCount > answeredCount) {
                answeredQuestCountListeners.forEach { it.onAnsweredQuestCountIncreased() }
            } else {
                answeredQuestCountListeners.forEach { it.onAnsweredQuestCountDecreased() }
            }
            answeredCount = newAnsweredQuestCount
        }
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
    if (statusIn != null && statusIn.isNotEmpty()) {
        if (statusIn.size == 1) {
            add("$QUEST_STATUS = ?", statusIn.single().name)
        } else {
            val names = statusIn.joinToString(",") { "\"$it\"" }
            add("$QUEST_STATUS IN ($names)")
        }
    }
    if (questTypes != null && questTypes.isNotEmpty()) {
        if (questTypes.size == 1) {
            add("$QUEST_TYPE = ?", questTypes.single())
        } else {
            val names = questTypes.joinToString(",") { "\"$it\"" }
            add("$QUEST_TYPE IN ($names)")
        }
    }
    if (bounds != null) {
        add(
            "(${ElementGeometryTable.Columns.LATITUDE} BETWEEN ? AND ?)",
            bounds.minLatitude.toString(),
            bounds.maxLatitude.toString()
        )
        add(
            "(${ElementGeometryTable.Columns.LONGITUDE} BETWEEN ? AND ?)",
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
) : ObjectRelationalMapping<OsmQuest> {

    override fun toContentValues(obj: OsmQuest) =
        toConstantContentValues(obj) + toUpdatableContentValues(obj)

    override fun toObject(cursor: Cursor) = OsmQuest(
            cursor.getLong(QUEST_ID),
            questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
            cursor.getLong(ELEMENT_ID),
            valueOf(cursor.getString(QUEST_STATUS)),
            cursor.getBlobOrNull(TAG_CHANGES)?.let { serializer.toObject<StringMapChanges>(it) },
            cursor.getStringOrNull(CHANGES_SOURCE),
            Date(cursor.getLong(LAST_UPDATE)),
            elementGeometryMapping.toObject(cursor)
    )

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
