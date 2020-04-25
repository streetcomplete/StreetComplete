package de.westnordost.streetcomplete.data.osm.splitway

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.NAME
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.Columns.QUEST_ID
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.Columns.SPLITS
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable.Columns.WAY_ID
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

/** Stores OsmQuestSplitWay objects by quest ID - the solutions of "differs along the way" quest
 *  answers. */
@Singleton class OsmQuestSplitWayDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmQuestSplitWayMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedSplitWay()
        fun onDeletedSplitWay()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getAll(): List<OsmQuestSplitWay> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun get(questId: Long): OsmQuestSplitWay? {
        val selection = "$QUEST_ID = ?"
        val args = arrayOf(questId.toString())
        return db.queryOne(NAME, null, selection, args) { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun add(quest: OsmQuestSplitWay) {
        db.insertOrThrow(NAME, null, mapping.toContentValues(quest))
        listeners.forEach { it.onAddedSplitWay() }
    }

    fun delete(questId: Long): Boolean {
        val result = db.delete(NAME, "$QUEST_ID = ?", arrayOf(questId.toString())) == 1
        if (result) listeners.forEach { it.onDeletedSplitWay() }
        return result
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

class OsmQuestSplitWayMapping @Inject constructor(
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry
) : ObjectRelationalMapping<OsmQuestSplitWay> {

    override fun toContentValues(obj: OsmQuestSplitWay) = contentValuesOf(
        QUEST_ID to obj.questId,
        QUEST_TYPE to obj.questType.javaClass.simpleName,
        WAY_ID to obj.wayId,
        SOURCE to obj.source,
        SPLITS to serializer.toBytes(ArrayList(obj.splits))
    )

    override fun toObject(cursor: Cursor)= OsmQuestSplitWay(
            cursor.getLong(QUEST_ID),
            questTypeList.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
            cursor.getLong(WAY_ID),
            cursor.getString(SOURCE),
            (serializer.toObject(cursor.getBlob(SPLITS)) as ArrayList<SplitPolylineAtPosition>)
    )
}
