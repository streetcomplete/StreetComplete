package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.data.*
import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import java.util.Date

class RelationDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: RelationDao

    @Before fun createDao() {
        dao = RelationDao(database, serializer)
    }

    @Test fun putGetNoTags() {
        val members = listOf(
            OsmRelationMember(0, "outer", Element.Type.WAY),
            OsmRelationMember(1, "inner", Element.Type.WAY)
        )
        val relation = rel(5, 1, members, null)
        dao.put(relation)
        val dbRelation = dao.get(5)

        checkEqual(relation, dbRelation!!)
    }

    @Test fun putGetWithTags() {
        val members = listOf(
            OsmRelationMember(0, "outer", Element.Type.WAY),
            OsmRelationMember(1, "inner", Element.Type.WAY)
        )
        val relation = rel(5, 1, members, mapOf("a key" to "a value"))
        dao.put(relation)
        val dbRelation = dao.get(5)

        checkEqual(relation, dbRelation!!)
    }

    @Test fun putOverwrites() {
        dao.put(rel(6, 0))
        dao.put(rel(6, 5))
        assertEquals(5, dao.get(6)!!.version)
    }

    @Test fun putOverwritesAlsoRelationMembers() {
        val members1 = listOf(
            OsmRelationMember(0, "outer", Element.Type.WAY),
            OsmRelationMember(1, "inner", Element.Type.WAY)
        )
        val members2 = listOf(
            OsmRelationMember(2, "outer", Element.Type.WAY),
            OsmRelationMember(3, "inner", Element.Type.WAY)
        )

        dao.put(rel(0, members = members1))
        dao.put(rel(0, members = members2))
        assertEquals(members2, dao.get(0)!!.members)
    }

    @Test fun getNull() {
        assertNull(dao.get(6))
    }

    @Test fun delete() {
        assertFalse(dao.delete(6))
        dao.put(rel(6))
        assertTrue(dao.delete(6))
        assertNull(dao.get(6))
        assertFalse(dao.delete(6))
    }

    @Test fun putAll() {
        dao.putAll(listOf(rel(1), rel(2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun getAll() {
        val e1 = rel(1, members = listOf(OsmRelationMember(0, "bla", Element.Type.NODE)))
        val e2 = rel(2, members = listOf(
            OsmRelationMember(0, "bla", Element.Type.NODE),
            OsmRelationMember(1, "blub", Element.Type.WAY),
        ))
        val e3 = rel(3, members = listOf(
            OsmRelationMember(3, "one", Element.Type.RELATION),
            OsmRelationMember(4, "two", Element.Type.WAY),
        ))
        dao.putAll(listOf(e1,e2,e3))
        assertEquals(
            listOf(e1, e2).map { it.id },
            dao.getAll(listOf(1,2,4)).sortedBy { it.id }.map { it.id }
        )
        assertEquals(
            listOf(e1, e2, e3).map { it.members },
            dao.getAll(listOf(1,2,3)).sortedBy { it.id }.map { it.members }
        )
    }

    @Test fun deleteAll() {
        dao.putAll(listOf(rel(1), rel(2), rel(3)))
        assertEquals(2, dao.deleteAll(listOf(1,2,4)))
        assertNotNull(dao.get(3))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
    }

    @Test fun getAllForElement() {
        val e1 = rel(1, members = listOf(OsmRelationMember(0, "bla", Element.Type.NODE)))
        val e2 = rel(2, members = listOf(
            OsmRelationMember(0, "bla", Element.Type.NODE),
            OsmRelationMember(1, "blub", Element.Type.WAY),
        ))
        val e3 = rel(3, members = listOf(
            OsmRelationMember(3, "one", Element.Type.RELATION),
            OsmRelationMember(4, "two", Element.Type.WAY),
        ))
        dao.putAll(listOf(e1,e2,e3))
        assertEquals(
            listOf(e1, e2).map { it.id },
            dao.getAllForNode(0).sortedBy { it.id }.map { it.id }
        )
        assertEquals(
            listOf(e3).map { it.id },
            dao.getAllForWay(4).sortedBy { it.id }.map { it.id }
        )
        assertEquals(
            listOf(e3).map { it.id },
            dao.getAllForRelation(3).sortedBy { it.id }.map { it.id }
        )
    }

    @Test fun getUnusedAndOldIds() {
        dao.putAll(listOf(rel(1L), rel(2L), rel(3L)))
        val unusedIds = dao.getIdsOlderThan(System.currentTimeMillis() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(1L, 2L, 3L)))
    }
}

private fun checkEqual(relation: Relation, dbRelation: Relation) {
    assertEquals(relation.id, dbRelation.id)
    assertEquals(relation.version.toLong(), dbRelation.version.toLong())
    assertEquals(relation.tags, dbRelation.tags)
    assertEquals(relation.members, dbRelation.members)
}

private fun rel(
    id: Long = 1L,
    version: Int = 1,
    members: List<RelationMember> = listOf(OsmRelationMember(1L, "", Element.Type.NODE)),
    tags: Map<String,String>? = emptyMap(),
    timestamp: Long = 123L
) = OsmRelation(id, version, members, tags, null, Date(timestamp))
