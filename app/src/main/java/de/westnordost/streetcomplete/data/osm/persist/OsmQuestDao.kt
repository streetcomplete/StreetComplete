package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import java.util.Date

import javax.inject.Inject
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.*
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.CHANGES_SOURCE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.QUEST_STATUS
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.Columns.TAG_CHANGES
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.data.QuestStatus.*
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer

class OsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmQuestMapping
) {

    private val db get() = dbHelper.writableDatabase

    fun add(quest: OsmQuest): Boolean {
        return addAll(listOf(quest)) == 1
    }

    fun get(id: Long): OsmQuest? {
        return db.queryOne(NAME_MERGED_VIEW, null, "$QUEST_ID = $id") { mapping.toObject(it) }
    }

    fun update(quest: OsmQuest): Boolean {
        quest.lastUpdate = Date()
        return db.update(NAME, mapping.toUpdatableContentValues(quest), "$QUEST_ID = ${quest.id}", null) == 1
    }

    fun delete(id: Long): Boolean {
        return db.delete(NAME, "$QUEST_ID = $id", null) == 1
    }

    fun addAll(quests: Collection<OsmQuest>): Int {
        var addedRows = 0
        db.transaction {
            for (quest in quests) {
                quest.lastUpdate = Date()
                val rowId = db.insert(NAME, null, mapping.toContentValues(quest))
                if (rowId != -1L) {
                    quest.id = rowId
                    addedRows++
                }
            }
        }
        return addedRows
    }

    fun deleteAllIds(ids: Collection<Long>): Int {
        return db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
    }

    fun unhideAll(): Int {
        val values = contentValuesOf(QUEST_STATUS to NEW.name)
        return db.update(NAME, values, "$QUEST_STATUS = ?", arrayOf(HIDDEN.name))
    }

    fun getLastSolved(): OsmQuest? {
        val qb = MergedQueryBuilder().apply { withStatusIn(HIDDEN, ANSWERED, CLOSED) }
        return db.queryOne(NAME_MERGED_VIEW, null, qb.selection, "$LAST_UPDATE DESC") { mapping.toObject(it) }
    }

    fun getAll(block: (MergedQueryBuilder.() -> Unit)? = null): List<OsmQuest> {
        val qb = block?.let { MergedQueryBuilder().apply(it) }
        return db.query(NAME_MERGED_VIEW, null, qb?.selection) { mapping.toObject(it) }
    }

    fun getAllIds(block: (MergedQueryBuilder.() -> Unit)? = null): List<Long> {
        val qb = block?.let { MergedQueryBuilder().apply(it) }
        return db.query(NAME_MERGED_VIEW, arrayOf(QUEST_ID), qb?.selection) { it.getLong(0) }
    }

    fun getCount(block: (MergedQueryBuilder.() -> Unit)? = null): Int {
        val qb = block?.let { MergedQueryBuilder().apply(it) }
        return db.queryOne(NAME_MERGED_VIEW, arrayOf("COUNT(*)"), qb?.selection) { it.getInt(0) } ?: 0
    }

    fun deleteAll(block: (QueryBuilder.() -> Unit)? = null): Int {
        val qb = block?.let { QueryBuilder().apply(it) }
        return db.delete(NAME, qb?.selection?.where, qb?.selection?.args)
    }

    open class QueryBuilder {
        internal val selection = WhereSelectionBuilder()

        fun forElement(elementType: Element.Type, elementId: Long) {
            selection.add("$ELEMENT_TYPE = ?", elementType.name)
            selection.add("$ELEMENT_ID = ?", elementId.toString())
        }

        fun withStatus(questStatus: QuestStatus) {
            selection.add("$QUEST_STATUS = ?", questStatus.name)
        }

        fun withStatusIn(vararg questStatus: QuestStatus) {
            val names = questStatus.joinToString(",") { "\"$it\"" }
            selection.add("$QUEST_STATUS IN ($names)")
        }

        fun forQuestTypeName(questType: String) {
            selection.add("$QUEST_TYPE = ?", questType)
        }

        fun forQuestTypeNames(questTypes: Collection<String>) {
            val names = questTypes.joinToString(",") { "\"$it\"" }
            selection.add("$QUEST_TYPE IN ($names)")
        }

        fun changedBefore(timestamp: Long) {
            selection.add("$LAST_UPDATE < ?", timestamp.toString())
        }
    }

    class MergedQueryBuilder : QueryBuilder() {
        fun withinBounds(bounds: BoundingBox) {
            selection.add("(${ElementGeometryTable.Columns.LATITUDE} BETWEEN ? AND ?)",
                bounds.minLatitude.toString(),
                bounds.maxLatitude.toString()
            )
            selection.add(
                "(${ElementGeometryTable.Columns.LONGITUDE} BETWEEN ? AND ?)",
                bounds.minLongitude.toString(),
                bounds.maxLongitude.toString()
            )
        }
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
        QuestStatus.valueOf(cursor.getString(QUEST_STATUS)),
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
