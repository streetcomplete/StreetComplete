package de.westnordost.streetcomplete.data.osm.mapdata

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.*

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.INDEX
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.REF
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.ROLE
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.TYPE
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.NAME_MEMBERS
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.lang.System.currentTimeMillis

/** Stores OSM relations */
class RelationDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
) {
    private val db get() = dbHelper.writableDatabase

    fun put(relation: Relation) {
        putAll(listOf(relation))
    }

    fun get(id: Long): Relation? =
        getAll(listOf(id)).firstOrNull()

    fun delete(id: Long): Boolean =
        deleteAll(listOf(id)) > 0

    fun putAll(relations: Collection<Relation>) {
        if (relations.isEmpty()) return
        val idsString = relations.joinToString(",") { it.id.toString() }
        db.transaction {
            db.delete(NAME_MEMBERS, "$ID IN ($idsString)", null)
            for (relation in relations) {
                relation.members.forEachIndexed { index, member ->
                    db.insertOrThrow(NAME_MEMBERS, null, contentValuesOf(
                        ID to relation.id,
                        INDEX to index,
                        REF to member.ref,
                        TYPE to member.type.name,
                        ROLE to member.role.orEmpty()
                    ))
                }
                db.replaceOrThrow(NAME, null, contentValuesOf(
                    ID to relation.id,
                    VERSION to relation.version,
                    TAGS to relation.tags?.let { serializer.toBytes(HashMap<String,String>(it)) },
                    LAST_UPDATE to currentTimeMillis()
                ))
            }
        }
    }

    fun getAll(ids: Collection<Long>): List<Relation> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")

        val membersByRelationId = mutableMapOf<Long, MutableList<RelationMember>>()
        db.query(NAME_MEMBERS, selection = "$ID IN ($idsString)", orderBy = "$ID, $INDEX") { c ->
            val members = membersByRelationId.getOrPut(c.getLong(ID)) { ArrayList() }
            members.add(OsmRelationMember(
                c.getLong(REF),
                c.getString(ROLE),
                Element.Type.valueOf(c.getString(TYPE))
            ))
        }

        return db.query(NAME, selection = "$ID IN ($idsString)") { c ->
            val id = c.getLong(ID)
            OsmRelation(
                id,
                c.getInt(VERSION),
                membersByRelationId.getValue(id),
                c.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.transaction {
            db.delete(NAME_MEMBERS, "$ID IN ($idsString)", null)
            db.delete(NAME, "ID IN ($idsString)", null)
        }
    }

    fun getAllForNode(nodeId: Long) : List<Relation> =
        getAllForElement(Element.Type.NODE, nodeId)

    fun getAllForWay(wayId: Long) : List<Relation> =
        getAllForElement(Element.Type.WAY, wayId)

    fun getAllForRelation(relationId: Long) : List<Relation> =
        getAllForElement(Element.Type.RELATION, relationId)

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), selection = "$LAST_UPDATE < $timestamp") { it.getLong(0) }

    private fun getAllForElement(elementType: Element.Type, elementId: Long): List<Relation> {
        val ids = db.query(
            NAME_MEMBERS,
            columns = arrayOf(ID),
            selection = "$REF = $elementId AND $TYPE = ?",
            selectionArgs = arrayOf(elementType.name)) { it.getLong(0) }.toSet()
        return getAll(ids)
    }
}
