package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import javax.inject.Inject

/** Persists OsmQuest objects, or more specifically, OsmQuestEntry objects */
class OsmQuestDao @Inject constructor(private val db: Database) {

    fun add(quest: OsmQuestDaoEntry): Boolean {
        val rowId = db.insertOrIgnore(NAME, quest.toPairs())
        if (rowId != -1L) {
            quest.id = rowId
            return true
        }
        return false
    }

    fun get(id: Long): OsmQuestDaoEntry? =
        db.queryOne(NAME, null, "$QUEST_ID = $id") { it.toOsmQuestEntry() }

    fun getAllInBBoxCount(bounds: BoundingBox): Int {
        return db.queryOne(NAME,
            columns = arrayOf("COUNT(*) as count"),
            where = inBoundsSql(bounds),
        ) { it.getInt("count") } ?: 0
    }

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$QUEST_ID = $id") == 1

    fun addAll(quests: List<OsmQuestDaoEntry>) {
        if (quests.isEmpty()) return
        val rowIds = db.insertOrIgnoreMany(NAME,
            arrayOf(QUEST_ID, QUEST_TYPE, ELEMENT_TYPE, ELEMENT_ID, LATITUDE, LONGITUDE),
            quests.map { arrayOf(
                it.id,
                it.questTypeName,
                it.elementType.name,
                it.elementId,
                it.position.latitude,
                it.position.longitude
            ) }
        )
        check(rowIds.size == quests.size)
        val rowIdsIt = rowIds.iterator()
        for (quest in quests) {
            quest.id = rowIdsIt.next().takeIf { it != -1L }
        }
    }

    fun getAllForElement(elementType: Element.Type, elementId: Long): List<OsmQuestDaoEntry> =
        db.query(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(elementType.name, elementId)
        ) { it.toOsmQuestEntry() }

    fun getAllInBBox(bounds: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuestDaoEntry> {
        var builder = inBoundsSql(bounds)
        if (questTypes != null) {
            if (questTypes.isEmpty()) return emptyList()
            val questTypesStr = questTypes.joinToString(",") { "'$it'" }
            builder += " AND $QUEST_TYPE IN (${questTypesStr})"
        }
        return db.query(NAME, where = builder) { it.toOsmQuestEntry() }
    }

    fun getAllIdsInBBox(bounds: BoundingBox): List<Long> {
        return db.query(NAME,
            columns = arrayOf(QUEST_ID),
            where = inBoundsSql(bounds),
        ) { it.getLong(QUEST_ID) }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$QUEST_ID IN (${ids.joinToString(",")})")
    }
}

private fun inBoundsSql(bbox: BoundingBox): String = """
        ($LATITUDE BETWEEN ${bbox.minLatitude} AND ${bbox.maxLatitude}) AND
        ($LONGITUDE BETWEEN ${bbox.minLongitude} AND ${bbox.maxLongitude})
    """.trimIndent()

private fun CursorPosition.toOsmQuestEntry(): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(
    getLong(QUEST_ID),
    getString(QUEST_TYPE),
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
)

private fun OsmQuestDaoEntry.toPairs() = listOf(
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
