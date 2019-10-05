package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.ObjectRelationalMapping

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

class RelationDao @Inject constructor(dbHelper: SQLiteOpenHelper, override val mapping: RelationMapping)
    : AOsmElementDao<Relation>(dbHelper) {

    override val tableName = NAME
    override val idColumnName = ID
    override val elementTypeName = Element.Type.RELATION.name
}

class RelationMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<Relation> {

    override fun toContentValues(obj: Relation) = contentValuesOf(
        ID to obj.id,
        VERSION to obj.version,
        MEMBERS to serializer.toBytes(ArrayList(obj.members)),
        TAGS to obj.tags?.let { serializer.toBytes(HashMap(it)) }
    )

    override fun toObject(cursor: Cursor) = OsmRelation(
        cursor.getLong(ID),
        cursor.getInt(VERSION),
        serializer.toObject<ArrayList<OsmRelationMember>>(cursor.getBlob(MEMBERS)) as List<RelationMember>,
        cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )
}
