package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable.NAME
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

/** Persists OsmQuest objects, or more specifically, OsmQuestEntry objects */
class OsmQuestDao(private val db: Database) {

    fun put(quest: OsmQuestDaoEntry) {
        db.replace(NAME, quest.toPairs())
    }

    fun get(key: OsmQuestKey): OsmQuestDaoEntry? =
        db.queryOne(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
        ) { it.toOsmQuestEntry() }

    fun delete(key: OsmQuestKey): Boolean =
        db.delete(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(key.elementType.name, key.elementId, key.questTypeName)
        ) == 1

    fun putAll(quests: Collection<OsmQuestDaoEntry>) {
        if (quests.isEmpty()) return
        // replace because even if the quest already exists in DB, the center position might have changed
        db.replaceMany(NAME,
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

    fun getAllForElements(keys: Collection<ElementKey>): List<OsmQuestDaoEntry> {
        if (keys.isEmpty()) return emptyList()
        return db.query(NAME,
                where = "$ELEMENT_ID IN (${keys.map { it.id }.joinToString(",")})",
            // this is faster than queryIn... even without ID index
            ) { it.toOsmQuestEntry() }.filter { ElementKey(it.elementType, it.elementId) in keys }
    }

    fun getAllInBBox(bounds: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuestDaoEntry> {
        var builder = inBoundsSql(bounds)
        if (questTypes != null) {
            if (questTypes.isEmpty()) return emptyList()
            val questTypesStr = questTypes.joinToString(",") { "'$it'" }
            builder += " AND $QUEST_TYPE IN ($questTypesStr)"
        }
        return db.query(NAME, where = builder) { it.toOsmQuestEntry() }
    }

    fun deleteAll(keys: Collection<OsmQuestKey>) {
        if (keys.isEmpty()) return
        db.transaction {
            for (key in keys) {
                delete(key)
            }
        }
    }

    fun clear() {
        db.delete(NAME)
    }
}

private fun inBoundsSql(bbox: BoundingBox): String = """
    ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
    ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
""".trimIndent()

private fun CursorPosition.toOsmQuestEntry(): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(
    ElementType.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getString(QUEST_TYPE),
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
)

private fun OsmQuestDaoEntry.toPairs() = listOf(
    QUEST_TYPE to questTypeName,
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId,
    LATITUDE to position.latitude,
    LONGITUDE to position.longitude
)

data class BasicOsmQuestDaoEntry(
    override val elementType: ElementType,
    override val elementId: Long,
    override val questTypeName: String,
    override val position: LatLon
) : OsmQuestDaoEntry
