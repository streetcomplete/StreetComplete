package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
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

class OsmQuestSplitWayDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry
) {
    fun getAll(): List<OsmQuestSplitWay> {
        return dbHelper.readableDatabase.query(NAME).use { cursor ->
            return cursor.map { it.createOsmQuestSplitWay() }
        }
    }

    fun get(id: Long): OsmQuestSplitWay? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(id.toString())
        return dbHelper.readableDatabase.queryOne(NAME, null, selection, args).use { cursor ->
            return cursor.createOsmQuestSplitWay()
        }
    }

    fun put(quest: OsmQuestSplitWay) {
        dbHelper.writableDatabase.insert(NAME, null, quest.createContentValues())
    }

    fun delete(id: Long) {
        dbHelper.writableDatabase.delete(NAME, "$QUEST_ID = $id", null)
    }

    private fun OsmQuestSplitWay.createContentValues() = ContentValues().also { v ->
        v.put(QUEST_ID, id)
        v.put(QUEST_TYPE, questType.javaClass.simpleName)
        v.put(WAY_ID, wayId)
        v.put(SOURCE, source)
        v.put(SPLITS, serializer.toBytes(ArrayList(splits)))
    }

    private fun Cursor.createOsmQuestSplitWay() = OsmQuestSplitWay(
        getLong(QUEST_ID),
        questTypeList.getByName(getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        getLong(WAY_ID),
        getString(SOURCE),
        (serializer.toObject(getBlob(SPLITS)) as ArrayList<SplitPolylineAtPosition>)
    )
}
