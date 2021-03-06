package de.westnordost.streetcomplete.data.osm.osmquests

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryMapping
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import javax.inject.Inject

/** Persists OsmQuest objects */
class OsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: NewOsmQuestMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(quest: OsmQuest) {
        val rowId = db.insertWithOnConflict(NAME, null, mapping.toContentValues(quest), CONFLICT_IGNORE)
        if (rowId != -1L) quest.id = rowId
    }

    fun get(id: Long): OsmQuest? {
        return db.queryOne(NAME_MERGED_VIEW, null, "$QUEST_ID = $id") { mapping.toObject(it) }
    }

    fun get(key: OsmQuestKey): OsmQuest? {
        return db.queryOne(
            NAME_MERGED_VIEW,
            null,
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            arrayOf(key.elementType.name, key.elementId.toString(), key.questTypeName)
        ) { mapping.toObject(it) }
    }

    fun getAllInBBoxCount(bounds: BoundingBox): Int {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        return db.queryOne(NAME_MERGED_VIEW, arrayOf("COUNT(*)"), builder.where, builder.args) { it.getInt(0) } ?: 0
    }

    fun delete(id: Long) {
        db.delete(NAME, "$QUEST_ID = $id", null)
    }

    fun addAll(quests: Collection<OsmQuest>) {
        if (quests.isEmpty()) return
        db.transaction {
            for (quest in quests) {
                add(quest)
            }
        }
    }

    fun getAllForElement(elementType: Element.Type, elementId: Long): List<OsmQuest> {
        return db.query(
            NAME_MERGED_VIEW,
            null,
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            arrayOf(elementType.name, elementId.toString())
        ) { mapping.toObject(it) }
    }

    fun getAllInBBox(bounds: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuest> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        if (questTypes != null) {
            builder.appendQuestTypes(questTypes)
        }
        return db.query(NAME_MERGED_VIEW, null, builder.where, builder.args) { mapping.toObject(it) }
    }

    fun getAllIdsInBBox(bounds: BoundingBox): List<Long> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        return db.query(NAME_MERGED_VIEW, arrayOf(QUEST_ID), builder.where, builder.args) { it.getLong(0) }
    }

    fun deleteAll(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
    }
}

private fun WhereSelectionBuilder.appendBounds(bbox: BoundingBox) {
    add("($CENTER_LATITUDE BETWEEN ? AND ?)",
        bbox.minLatitude.toString(),
        bbox.maxLatitude.toString()
    )
    add(
        "($CENTER_LONGITUDE BETWEEN ? AND ?)",
        bbox.minLongitude.toString(),
        bbox.maxLongitude.toString()
    )
}

private fun WhereSelectionBuilder.appendQuestTypes(questTypes: Collection<String>) {
    require(questTypes.isNotEmpty()) { "questTypes must not be empty" }
    val names = questTypes.joinToString(",") { "\"$it\"" }
    add("$QUEST_TYPE IN ($names)")
}

class NewOsmQuestMapping @Inject constructor(
    private val questTypeRegistry: QuestTypeRegistry,
    private val elementGeometryMapping: ElementGeometryMapping
) : ObjectRelationalMapping<OsmQuest> {

    override fun toContentValues(obj: OsmQuest) = contentValuesOf(
        QUEST_ID to obj.id,
        QUEST_TYPE to obj.type::class.simpleName!!,
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId
    )

    override fun toObject(cursor: Cursor): OsmQuest {
        return OsmQuest(
            cursor.getLong(QUEST_ID),
            questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
            cursor.getLong(ELEMENT_ID),
            elementGeometryMapping.toObject(cursor)
        )
    }
}
