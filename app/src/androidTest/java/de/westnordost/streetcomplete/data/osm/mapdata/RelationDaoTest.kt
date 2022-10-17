package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RelationDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: RelationDao

    @Before fun createDao() {
        dao = RelationDao(database)
    }

    @Test fun putGetNoTags() {
        val members = listOf(
            RelationMember(WAY, 0, "outer"),
            RelationMember(WAY, 1, "inner")
        )
        val relation = rel(5, 1, members)
        dao.put(relation)
        val dbRelation = dao.get(5)

        assertEquals(relation, dbRelation!!)
    }

    @Test fun putGetWithTags() {
        val members = listOf(
            RelationMember(WAY, 0, "outer"),
            RelationMember(WAY, 1, "inner")
        )
        val relation = rel(5, 1, members, mapOf("a key" to "a value"))
        dao.put(relation)
        val dbRelation = dao.get(5)

        assertEquals(relation, dbRelation!!)
    }

    @Test fun putOverwrites() {
        dao.put(rel(6, 0))
        dao.put(rel(6, 5))
        assertEquals(5, dao.get(6)!!.version)
    }

    @Test fun putOverwritesAlsoRelationMembers() {
        val members1 = listOf(
            RelationMember(WAY, 0, "outer"),
            RelationMember(WAY, 1, "inner")
        )
        val members2 = listOf(
            RelationMember(WAY, 2, "outer"),
            RelationMember(WAY, 3, "inner")
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
        val e1 = rel(1, members = listOf(RelationMember(NODE, 0, "bla")))
        val e2 = rel(2, members = listOf(
            RelationMember(NODE, 0, "bla"),
            RelationMember(WAY, 1, "blub"),
        ))
        val e3 = rel(3, members = listOf(
            RelationMember(RELATION, 3, "one"),
            RelationMember(WAY, 4, "two"),
        ))
        dao.putAll(listOf(e1, e2, e3))
        assertEquals(
            listOf(e1, e2),
            dao.getAll(listOf(1, 2, 4)).sortedBy { it.id }
        )
        assertEquals(
            listOf(e1, e2, e3),
            dao.getAll(listOf(1, 2, 3)).sortedBy { it.id }
        )
    }

    @Test fun deleteAll() {
        dao.putAll(listOf(rel(1), rel(2), rel(3)))
        assertEquals(2, dao.deleteAll(listOf(1, 2, 4)))
        assertNotNull(dao.get(3))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
    }

    @Test fun getAllForElement() {
        val e1 = rel(1, members = listOf(RelationMember(NODE, 0, "bla")))
        val e2 = rel(2, members = listOf(
            RelationMember(NODE, 0, "bla"),
            RelationMember(WAY, 1, "blub"),
        ))
        val e3 = rel(3, members = listOf(
            RelationMember(RELATION, 3, "one"),
            RelationMember(WAY, 4, "two"),
        ))
        dao.putAll(listOf(e1, e2, e3))
        assertEquals(
            listOf(e1, e2),
            dao.getAllForNode(0).sortedBy { it.id }
        )
        assertEquals(
            listOf(e3),
            dao.getAllForWay(4).sortedBy { it.id }
        )
        assertEquals(
            listOf(e3),
            dao.getAllForRelation(3).sortedBy { it.id }
        )

        assertEquals(
            listOf(e1, e2, e3),
            dao.getAllForElements(
                nodeIds = listOf(0),
                wayIds = listOf(1),
                relationIds = listOf(3)
            ).sortedBy { it.id }
        )
    }

    @Test fun getUnusedAndOldIds() {
        dao.putAll(listOf(rel(1L), rel(2L), rel(3L)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(1L, 2L, 3L)))
    }

    @Test fun getUnusedAndOldIdsButAtMostX() {
        dao.putAll(listOf(rel(1L), rel(2L), rel(3L)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10, 2)
        assertEquals(2, unusedIds.size)
    }

    @Test fun clear() {
        dao.putAll(listOf(rel(1L), rel(2L), rel(3L)))
        dao.clear()
        assertTrue(dao.getAll(listOf(1L, 2L, 3L)).isEmpty())
    }
}

private fun rel(
    id: Long = 1L,
    version: Int = 1,
    members: List<RelationMember> = listOf(RelationMember(NODE, 1L, "")),
    tags: Map<String, String> = emptyMap(),
    timestamp: Long = 123L
) = Relation(id, members.toMutableList(), tags, version, timestamp)
