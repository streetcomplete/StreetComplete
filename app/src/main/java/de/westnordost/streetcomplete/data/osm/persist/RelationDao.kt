package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.*

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.data.osm.persist.RelationTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.persist.RelationTable.Columns.MEMBERS
import de.westnordost.streetcomplete.data.osm.persist.RelationTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.persist.RelationTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.persist.RelationTable.NAME
import de.westnordost.streetcomplete.ktx.*

class RelationDao @Inject constructor(dbHelper: SQLiteOpenHelper, private val serializer: Serializer)
	: AOsmElementDao<Relation>(dbHelper) {

    override val tableName = NAME
    override val idColumnName = ID
    override val elementTypeName = Element.Type.RELATION.name

    override fun createContentValuesFrom(element: Relation) = contentValuesOf(
	    ID to element.id,
	    VERSION to element.version,
	    MEMBERS to serializer.toBytes(ArrayList(element.members)),
	    TAGS to element.tags?.let { serializer.toBytes(HashMap(it)) }
    )

    override fun createObjectFrom(cursor: Cursor): Relation = OsmRelation(
	    cursor.getLong(ID),
	    cursor.getInt(VERSION),
	    serializer.toObject<ArrayList<OsmRelationMember>>(cursor.getBlob(MEMBERS)) as List<RelationMember>,
	    cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )
}
