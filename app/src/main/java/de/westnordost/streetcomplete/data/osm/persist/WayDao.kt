package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.persist.WayTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.persist.WayTable.Columns.NODE_IDS
import de.westnordost.streetcomplete.data.osm.persist.WayTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.persist.WayTable.Columns.VERSION
import de.westnordost.streetcomplete.ktx.*

class WayDao @Inject constructor(
	private val dbHelper: SQLiteOpenHelper,
	private val serializer: Serializer) :
	AOsmElementDao<Way>(dbHelper) {

	private val db get() = dbHelper.writableDatabase

    override val tableName = WayTable.NAME
    override val idColumnName = ID
    override val elementTypeName = Element.Type.WAY.name

    override fun createContentValuesFrom(element: Way) = contentValuesOf(
        ID to element.id,
        VERSION to element.version,
        NODE_IDS to serializer.toBytes(ArrayList(element.nodeIds)),
        TAGS to element.tags?.let { serializer.toBytes(HashMap(it)) }
    )

    override fun createObjectFrom(cursor: Cursor): Way = OsmWay(
	    cursor.getLong(ID),
	    cursor.getInt(VERSION),
	    serializer.toObject<ArrayList<Long>>(cursor.getBlob(NODE_IDS)),
	    cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )

    /** Cleans up element entries that are not referenced by any quest anymore.  */
    override fun deleteUnreferenced() {
	    val where = """
			$idColumnName NOT IN (
			${getSelectAllElementIdsIn(OsmQuestTable.NAME)} 
			UNION
			${getSelectAllElementIdsIn(UndoOsmQuestTable.NAME)}
			UNION
			SELECT ${OsmQuestSplitWayTable.Columns.WAY_ID} AS $idColumnName FROM ${OsmQuestSplitWayTable.NAME}
			)""".trimIndent()

        db.delete(tableName, where, null)
    }
}
