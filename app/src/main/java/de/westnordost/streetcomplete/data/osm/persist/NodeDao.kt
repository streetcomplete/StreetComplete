package de.westnordost.streetcomplete.data.osm.persist


import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element

import java.util.HashMap

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.NAME
import de.westnordost.streetcomplete.ktx.*

class NodeDao @Inject constructor(dbHelper: SQLiteOpenHelper, private val serializer: Serializer) :
    AOsmElementDao<Node>(dbHelper) {

    override val tableName = NAME
    override val idColumnName = ID
    override val elementTypeName = Element.Type.NODE.name

    override fun createContentValuesFrom(element: Node) = contentValuesOf(
	    ID to element.id,
	    VERSION to element.version,
	    LATITUDE to element.position.latitude,
	    LONGITUDE to element.position.longitude,
	    TAGS to element.tags?.let { serializer.toBytes(HashMap(it)) }
    )

    override fun createObjectFrom(cursor: Cursor): Node = OsmNode(
        cursor.getLong(ID),
        cursor.getInt(VERSION),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
        cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )
}
