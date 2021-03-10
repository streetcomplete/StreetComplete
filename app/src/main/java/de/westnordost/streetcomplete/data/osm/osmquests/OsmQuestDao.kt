package de.westnordost.streetcomplete.data.osm.osmquests

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import de.westnordost.streetcomplete.ktx.*
import javax.inject.Inject

/** Persists OsmQuest objects, or more specifically, OsmQuestEntry objects */
class OsmQuestDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun add(quest: OsmQuestDaoEntry): Boolean {
        val rowId = db.insertWithOnConflict(NAME, null, quest.toContentValues(), CONFLICT_IGNORE)
        if (rowId != -1L) {
            quest.id = rowId
            return true
        }
        return false
    }

    fun get(id: Long): OsmQuestDaoEntry? =
        db.queryOne(NAME, null, "$QUEST_ID = $id") { it.toOsmQuestEntry() }

    fun getAllInBBoxCount(bounds: BoundingBox): Int {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        return db.queryOne(NAME, arrayOf("COUNT(*)"), builder.where, builder.args) { it.getInt(0) } ?: 0
    }

    fun delete(id: Long): Boolean {
        return db.delete(NAME, "$QUEST_ID = $id", null) == 1
    }

    fun addAll(quests: Collection<OsmQuestDaoEntry>): Int {
        if (quests.isEmpty()) return 0
        var addedCount = 0
        db.transaction {
            for (quest in quests) {
                if (add(quest)) addedCount++
            }
        }
        return addedCount
    }

    fun getAllForElement(elementType: Element.Type, elementId: Long): List<OsmQuestDaoEntry> {
        return db.query(NAME,
            selection = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            selectionArgs = arrayOf(elementType.name, elementId.toString())
        ) { it.toOsmQuestEntry() }
    }

    fun getAllInBBox(bounds: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuestDaoEntry> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        if (questTypes != null) {
            builder.appendQuestTypes(questTypes)
        }
        return db.query(NAME, null, builder.where, builder.args) { it.toOsmQuestEntry() }
    }

    fun getAllIdsInBBox(bounds: BoundingBox): List<Long> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bounds)
        return db.query(NAME, arrayOf(QUEST_ID), builder.where, builder.args) { it.getLong(0) }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})", null)
    }
}

private fun WhereSelectionBuilder.appendBounds(bbox: BoundingBox) {
    add("($LATITUDE BETWEEN ? AND ?)",
        bbox.minLatitude.toString(),
        bbox.maxLatitude.toString()
    )
    add(
        "($LONGITUDE BETWEEN ? AND ?)",
        bbox.minLongitude.toString(),
        bbox.maxLongitude.toString()
    )
}

private fun WhereSelectionBuilder.appendQuestTypes(questTypes: Collection<String>) {
    require(questTypes.isNotEmpty()) { "questTypes must not be empty" }
    val names = questTypes.joinToString(",") { "'$it'" }
    add("$QUEST_TYPE IN ($names)")
}

private fun Cursor.toOsmQuestEntry(): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(
    getLong(QUEST_ID),
    getString(QUEST_TYPE),
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
)

private fun OsmQuestDaoEntry.toContentValues() = contentValuesOf(
    QUEST_ID to id,
    QUEST_TYPE to questTypeName,
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId,
    LATITUDE to position.latitude,
    LONGITUDE to position.longitude
)

data class BasicOsmQuestDaoEntry(
    override var id: Long?,
    override val questTypeName: String,
    override val elementType: Element.Type,
    override val elementId: Long,
    override val position: LatLon
) : OsmQuestDaoEntry
