package de.westnordost.streetcomplete.data.osm.splitway

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.NAME
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.Columns.SOURCE
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.Columns.SPLITS
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable.Columns.WAY_ID
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

/** Stores OsmQuestSplitWay objects by quest ID - the solutions of "differs along the way" quest
 *  answers. */
@Singleton class SplitOsmWayDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: SplitOsmWayMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedSplitWay()
        fun onDeletedSplitWay()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun getAll(): List<SplitOsmWay> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun get(id: Long): SplitOsmWay? {
        val selection = "$ID = ?"
        val args = arrayOf(id.toString())
        return db.queryOne(NAME, null, selection, args) { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun add(split: SplitOsmWay) {
        val rowId = db.insertOrThrow(NAME, null, mapping.toContentValues(split))
        if (rowId != -1L) split.id = rowId
        listeners.forEach { it.onAddedSplitWay() }
    }

    fun delete(id: Long): Boolean {
        val result = db.delete(NAME, "$ID = ?", arrayOf(id.toString())) == 1
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

class SplitOsmWayMapping @Inject constructor(
    private val serializer: Serializer,
    private val questTypeList: QuestTypeRegistry
) : ObjectRelationalMapping<SplitOsmWay> {

    override fun toContentValues(obj: SplitOsmWay) = contentValuesOf(
        ID to obj.id,
        QUEST_TYPE to obj.osmElementQuestType.javaClass.simpleName,
        WAY_ID to obj.wayId,
        SOURCE to obj.source,
        SPLITS to serializer.toBytes(ArrayList(obj.splits))
    )

    override fun toObject(cursor: Cursor)= SplitOsmWay(
        cursor.getLong(ID),
        questTypeList.getByName(cursor.getString(QUEST_TYPE)) as OsmElementQuestType<*>,
        cursor.getLong(WAY_ID),
        cursor.getString(SOURCE),
        (serializer.toObject(cursor.getBlob(SPLITS)) as ArrayList<SplitPolylineAtPosition>)
    )
}
