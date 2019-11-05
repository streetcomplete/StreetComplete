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
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.persist.NodeTable.NAME
import de.westnordost.streetcomplete.ktx.*

class NodeDao @Inject constructor(dbHelper: SQLiteOpenHelper, override val mapping: NodeMapping)
    : AOsmElementDao<Node>(dbHelper) {

    override val tableName = NAME
    override val idColumnName = ID
    override val elementTypeName = Element.Type.NODE.name
}

class NodeMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<Node> {

    override fun toContentValues(obj: Node) = contentValuesOf(
        ID to obj.id,
        VERSION to obj.version,
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude,
        TAGS to obj.tags?.let { serializer.toBytes(HashMap(it)) }
    )

    override fun toObject(cursor: Cursor) = OsmNode(
        cursor.getLong(ID),
        cursor.getInt(VERSION),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
        cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )
}
