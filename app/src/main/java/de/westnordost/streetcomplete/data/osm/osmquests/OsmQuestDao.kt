package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import javax.inject.Inject

/** Persists OsmQuest objects, or more specifically, OsmQuestEntry objects */
class OsmQuestDao @Inject constructor(private val db: Database) {

    fun put(quest: OsmQuestDaoEntry) {
        db.replace(NAME, quest.toPairs())
    }

    fun get(key: OsmQuestKey): OsmQuestDaoEntry? =
        db.queryOne(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
        ) { it.toOsmQuestEntry() }

    fun getAllInBBoxCount(bounds: BoundingBox): Int {
        return db.queryOne(NAME,
            columns = arrayOf("COUNT(*) as count"),
            where = inBoundsSql(bounds),
        ) { it.getInt("count") } ?: 0
    }

    fun delete(key: OsmQuestKey): Boolean =
        db.delete(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
        ) == 1

    fun putAll(quests: Collection<OsmQuestDaoEntry>) {
        if (quests.isEmpty()) return
        db.insertOrIgnoreMany(NAME,
            arrayOf(QUEST_TYPE, ELEMENT_TYPE, ELEMENT_ID, LATITUDE, LONGITUDE),
            quests.map { arrayOf(
                it.questTypeName,
                it.elementType.name,
                it.elementId,
                it.position.latitude,
                it.position.longitude
            ) }
        )
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

    fun deleteAll(keys: Collection<OsmQuestKey>) {
        if (keys.isEmpty()) return
        db.transaction {
            for(key in keys) {
                delete(key)
            }
        }
    }
}

private fun inBoundsSql(bbox: BoundingBox): String = """
        ($LATITUDE BETWEEN ${bbox.minLatitude} AND ${bbox.maxLatitude}) AND
        ($LONGITUDE BETWEEN ${bbox.minLongitude} AND ${bbox.maxLongitude})
    """.trimIndent()

private fun CursorPosition.toOsmQuestEntry(): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getString(QUEST_TYPE),
    OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
)

private fun OsmQuestDaoEntry.toPairs() = listOf(
    QUEST_TYPE to questTypeName,
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId,
    LATITUDE to position.latitude,
    LONGITUDE to position.longitude
)

data class BasicOsmQuestDaoEntry(
    override val elementType: Element.Type,
    override val elementId: Long,
    override val questTypeName: String,
    override val position: LatLon
) : OsmQuestDaoEntry
