package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.Database

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.INDEX
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.REF
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.ROLE
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.TYPE
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables.NAME_MEMBERS
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.lang.System.currentTimeMillis
import java.util.Date

/** Stores OSM relations */
class RelationDao @Inject constructor(
    private val db: Database,
    private val serializer: Serializer
) {
    fun put(relation: Relation) {
        putAll(listOf(relation))
    }

    fun get(id: Long): Relation? =
        getAll(listOf(id)).firstOrNull()

    fun delete(id: Long): Boolean =
        deleteAll(listOf(id)) == 1

    fun putAll(relations: Collection<Relation>) {
        if (relations.isEmpty()) return
        val idsString = relations.joinToString(",") { it.id.toString() }

        val time = currentTimeMillis()

        db.transaction {
            db.delete(NAME_MEMBERS, "$ID IN ($idsString)")

            db.insertMany(NAME_MEMBERS,
                arrayOf(ID, INDEX, REF, TYPE, ROLE),
                relations.flatMap { relation ->
                    relation.members.mapIndexed { index, member ->
                        arrayOf(
                            relation.id,
                            index,
                            member.ref,
                            member.type.name,
                            member.role.orEmpty()
                        )
                    }
                }
            )
            db.replaceMany(NAME,
                arrayOf(ID, VERSION, TAGS, TIMESTAMP, LAST_SYNC),
                relations.map { relation ->
                    arrayOf(
                        relation.id,
                        relation.version,
                        relation.tags?.let { serializer.toBytes(HashMap<String,String>(it)) },
                        relation.dateEdited.time,
                        time
                    )
                }
            )
        }
    }

    fun getAll(ids: Collection<Long>): List<Relation> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")

        val membersByRelationId = mutableMapOf<Long, MutableList<RelationMember>>()
        db.query(NAME_MEMBERS, where = "$ID IN ($idsString)", orderBy = "$ID, $INDEX") { c ->
            val members = membersByRelationId.getOrPut(c.getLong(ID)) { ArrayList() }
            members.add(OsmRelationMember(
                c.getLong(REF),
                c.getString(ROLE),
                Element.Type.valueOf(c.getString(TYPE))
            ))
        }

        return db.query(NAME, where = "$ID IN ($idsString)") { c ->
            val id = c.getLong(ID)
            OsmRelation(
                id,
                c.getInt(VERSION),
                membersByRelationId.getValue(id),
                c.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) },
                null,
                Date(c.getLong(TIMESTAMP))
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.transaction {
            db.delete(NAME_MEMBERS, "$ID IN ($idsString)")
            db.delete(NAME, "$ID IN ($idsString)")
        }
    }

    fun getAllForNode(nodeId: Long) : List<Relation> =
        getAllForElement(Element.Type.NODE, nodeId)

    fun getAllForWay(wayId: Long) : List<Relation> =
        getAllForElement(Element.Type.WAY, wayId)

    fun getAllForRelation(relationId: Long) : List<Relation> =
        getAllForElement(Element.Type.RELATION, relationId)

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), where = "$LAST_SYNC < $timestamp") { it.getLong(ID) }

    private fun getAllForElement(elementType: Element.Type, elementId: Long): List<Relation> {
        val ids = db.query(
            NAME_MEMBERS,
            columns = arrayOf(ID),
            where = "$REF = $elementId AND $TYPE = ?",
            args = arrayOf(elementType.name)) { it.getLong(ID) }.toSet()
        return getAll(ids)
    }
}
