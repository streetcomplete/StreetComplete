package de.westnordost.streetcomplete.data.osm.osmquest

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryMapping
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.NAME
import de.westnordost.streetcomplete.data.osm.osmquest.NewOsmQuestTable.NAME_MERGED_VIEW
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.*
import javax.inject.Inject

/** Persists OsmQuest objects */
class NewOsmQuestDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: NewOsmQuestMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(quest: NewOsmQuest) {
        val rowId = db.insertWithOnConflict(NAME, null, mapping.toContentValues(quest), CONFLICT_IGNORE)
        if (rowId != -1L) quest.id = rowId
    }

    fun get(id: Long): NewOsmQuest? {
        return db.queryOne(NAME_MERGED_VIEW, null, "$QUEST_ID = $id") { mapping.toObject(it) }
    }

    fun delete(id: Long) {
        db.delete(NAME, "$QUEST_ID = $id", null)
    }

    fun addAll(quests: Collection<NewOsmQuest>) {
        if (quests.isEmpty()) return
        db.transaction {
            for (quest in quests) {
                add(quest)
            }
        }
    }

    fun getAllForElement(elementType: Element.Type, elementId: Long): List<NewOsmQuest> {
        return db.query(
            NAME_MERGED_VIEW,
            null,
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            arrayOf(elementType.name, elementId.toString())
        ) { mapping.toObject(it) }
    }

    fun getAllInBBox(bounds: BoundingBox): List<NewOsmQuest> {
        return db.query(
            NAME_MERGED_VIEW,
            null,
            "($CENTER_LATITUDE BETWEEN ? AND ?) AND ($CENTER_LONGITUDE BETWEEN ? AND ?)",
            arrayOf(
                bounds.minLatitude.toString(),
                bounds.maxLatitude.toString(),
                bounds.minLongitude.toString(),
                bounds.maxLongitude.toString()
            )
        ) { mapping.toObject(it) }
    }

    fun deleteAll(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
    }
}

class NewOsmQuestMapping @Inject constructor(
    private val questTypeRegistry: QuestTypeRegistry,
    private val elementGeometryMapping: ElementGeometryMapping
) : ObjectRelationalMapping<NewOsmQuest> {

    override fun toContentValues(obj: NewOsmQuest) = contentValuesOf(
        QUEST_ID to obj.id,
        QUEST_TYPE to obj.type.javaClass.simpleName,
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId
    )

    override fun toObject(cursor: Cursor): NewOsmQuest {
        return NewOsmQuest(
            cursor.getLong(QUEST_ID),
            questTypeRegistry.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
            cursor.getLong(ELEMENT_ID),
            elementGeometryMapping.toObject(cursor)
        )
    }
}
