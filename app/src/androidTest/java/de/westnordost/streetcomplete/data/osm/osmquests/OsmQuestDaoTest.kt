package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OsmQuestDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmQuestDao

    @Before fun createDao() {
        dao = OsmQuestDao(database)
    }

    @Test fun addGet() {
        assertNull(dao.get(1L))
        val q = entry()
        dao.add(q)
        assertNotNull(q.id)
        assertEquals(q, dao.get(q.id!!))
    }

    @Test fun delete() {
        val q = entry()
        dao.add(q)
        assertFalse(dao.delete(123))
        assertNotNull(q.id)
        assertTrue(dao.delete(q.id!!))
        assertNull(dao.get(q.id!!))
    }

    @Test fun deleteAll() {
        val q1 = entry("a")
        val q2 = entry("b")
        val q3 = entry("c")
        dao.addAll(listOf(q1, q2, q3))
        assertEquals(2, dao.deleteAll(listOf(q1.id!!, q2.id!!)))

        assertNull(dao.get(q1.id!!))
        assertNull(dao.get(q2.id!!))
        assertEquals(q3, dao.get(q3.id!!))
    }

    @Test fun getAllForElement() {
        val q1 = entry("a", Element.Type.NODE, 0)
        val q2 = entry("b", Element.Type.NODE, 0)

        dao.addAll(listOf(
            q1, q2,
            entry("a", Element.Type.WAY, 0L),
            entry("a", Element.Type.NODE, 1L)
        ))
        assertTrue(dao.getAllForElement(Element.Type.NODE, 0L).containsExactlyInAnyOrder(listOf(q1,q2)))
    }

    @Test fun getAllInBBox() {
        // in
        val q1 = entry("a", Element.Type.NODE, 0L, p(0.0,0.0))
        val q2 = entry("a", Element.Type.NODE, 1L, p(1.0,1.0))
        val q3 = entry("a", Element.Type.NODE, 2L, p(0.5,0.5))

        dao.addAll(listOf(
            q1, q2, q3,
            // in but wrong quest type
            entry("b", Element.Type.NODE, 3L, p(0.5,0.5)),
            // out
            entry("a", Element.Type.NODE, 4L, p(-0.5,0.5)),
            entry("a", Element.Type.NODE, 5L, p(0.5,-0.5)),
            entry("a", Element.Type.NODE, 6L, p(0.5,1.5)),
            entry("a", Element.Type.NODE, 7L, p(1.5,0.5)),
        ))

        assertTrue(dao.getAllInBBox(
            BoundingBox(0.0,0.0,1.0,1.0),
            listOf("a")
        ).containsExactlyInAnyOrder(listOf(q1,q2,q3)))
    }

    @Test fun getAllInBBoxCount() {
        // in
        val q1 = entry("a", Element.Type.NODE, 0L, p(0.0,0.0))
        val q2 = entry("a", Element.Type.NODE, 1L, p(1.0,1.0))
        val q3 = entry("a", Element.Type.NODE, 2L, p(0.5,0.5))
        // out
        dao.addAll(listOf(
            q1, q2, q3,
            // out
            entry("a", Element.Type.NODE, 4L, p(-0.5,0.5)),
            entry("a", Element.Type.NODE, 5L, p(0.5,-0.5)),
            entry("a", Element.Type.NODE, 6L, p(0.5,1.5)),
            entry("a", Element.Type.NODE, 7L, p(1.5,0.5)),
        ))
        assertEquals(3, dao.getAllInBBoxCount(BoundingBox(0.0,0.0,1.0,1.0)))
    }

    private fun entry(
        questTypeName: String = "a",
        elementType: Element.Type = Element.Type.NODE,
        elementId: Long = 0L,
        pos: LatLon = p(0.0,0.0)
    ) = BasicOsmQuestDaoEntry(null, questTypeName, elementType, elementId, pos)

    private fun p(x: Double, y: Double): LatLon = OsmLatLon(y,x)
}
