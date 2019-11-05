package de.westnordost.streetcomplete.data.osm.persist

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.Relation

import org.junit.Assert.assertEquals

class RelationDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: RelationDao

    @Before fun createDao() {
        dao = RelationDao(dbHelper, RelationMapping(serializer))
    }

    @Test fun putGetNoTags() {
        val members = listOf(
            OsmRelationMember(0, "outer", Element.Type.WAY),
            OsmRelationMember(1, "inner", Element.Type.WAY)
        )
        val relation = OsmRelation(5, 1, members, null)
        dao.put(relation)
        val dbRelation = dao.get(5)

        checkEqual(relation, dbRelation!!)
    }

    @Test fun putGetWithTags() {
        val members = listOf(
            OsmRelationMember(0, "outer", Element.Type.WAY),
            OsmRelationMember(1, "inner", Element.Type.WAY)
        )
        val relation = OsmRelation(5, 1, members, mapOf("a key" to "a value"))
        dao.put(relation)
        val dbRelation = dao.get(5)

        checkEqual(relation, dbRelation!!)
    }

    private fun checkEqual(relation: Relation, dbRelation: Relation) {
        assertEquals(relation.id, dbRelation.id)
        assertEquals(relation.version.toLong(), dbRelation.version.toLong())
        assertEquals(relation.tags, dbRelation.tags)
        assertEquals(relation.members, dbRelation.members)
    }
}
