package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.changes.SplitWay
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.NAME
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.Columns.SPLITS
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayTable.Columns.WAY_ID
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import javax.inject.Inject
import kotlin.collections.ArrayList

class SplitWayDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry
) {
    fun getAll(): List<OsmQuestSplitWay> {
        return dbHelper.readableDatabase.query(NAME).use { cursor ->
            return cursor.map { it.createOsmQuestSplitWay() }
        }
    }

    fun put(quest: OsmQuestSplitWay) {
        dbHelper.writableDatabase.insert(NAME, null, quest.createContentValues())
    }

    fun delete(id: Long): Int {
        return dbHelper.writableDatabase.delete(NAME, "$QUEST_ID = $id", null)
    }

    private fun OsmQuestSplitWay.createContentValues() = ContentValues().also { v ->
        v.put(QUEST_ID, id)
        v.put(QUEST_TYPE, questType.javaClass.simpleName)
        v.put(WAY_ID, wayId)
        v.put(SOURCE, source)
        v.put(SPLITS, serializer.toBytes(ArrayList(splits.map { it.toData() })))
    }

    private fun Cursor.createOsmQuestSplitWay() = OsmQuestSplitWay(
        getLong(QUEST_ID),
        questTypeList.getByName(getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        getLong(WAY_ID),
        getString(SOURCE),
        (serializer.toObject(getBlob(SPLITS)) as List<SplitWayData>).map { it.toSplitWay() }
    )
}

private fun SplitWay.toData() = SplitWayData(
    firstNode.id, firstNode.version, firstNode.position.latitude, firstNode.position.longitude,
    secondNode.id, secondNode.version, secondNode.position.latitude, secondNode.position.longitude,
    delta)

private fun SplitWayData.toSplitWay() = SplitWay(
    OsmNode(firstId, firstVersion, firstLat, firstLon, null),
    OsmNode(secondId, secondVersion, secondLat, secondLon, null),
    delta)

data class SplitWayData(
    val firstId: Long, val firstVersion: Int, val firstLat: Double, val firstLon: Double,
    val secondId: Long, val secondVersion: Int, val secondLat: Double, val secondLon: Double,
    val delta: Double)
