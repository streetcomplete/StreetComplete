package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsmQuestDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmQuestDao

    @BeforeTest fun createDao() {
        dao = OsmQuestDao(database)
    }

    @Test fun addGet() {
        val q = entry(ElementType.NODE, 123L, "a")
        val key = q.key
        assertNull(dao.get(key))
        dao.put(q)
        assertEquals(q, dao.get(key))
    }

    @Test fun delete() {
        val q = entry()
        assertFalse(dao.delete(q.key))
        dao.put(q)
        assertTrue(dao.delete(q.key))
        assertNull(dao.get(q.key))
    }

    @Test fun deleteAll() {
        val q1 = entry(questTypeName = "a")
        val q2 = entry(questTypeName = "b")
        val q3 = entry(questTypeName = "c")
        dao.putAll(listOf(q1, q2, q3))
        dao.deleteAll(listOf(q1.key, q2.key))

        assertNull(dao.get(q1.key))
        assertNull(dao.get(q2.key))
        assertEquals(q3, dao.get(q3.key))
    }

    @Test fun clear() {
        val q1 = entry(questTypeName = "a")
        dao.put(q1)
        dao.clear()
        assertNull(dao.get(q1.key))
    }

    @Test fun getAllForElements() {
        val q1 = entry(ElementType.NODE, 0, "a")
        val q2 = entry(ElementType.NODE, 0, "b")
        val q3 = entry(ElementType.NODE, 1L, "a")

        dao.putAll(listOf(
            q1, q2, q3,
            entry(ElementType.WAY, 0L, "a"),
            entry(ElementType.NODE, 2L, "a")
        ))

        val keys = listOf(
            ElementKey(ElementType.NODE, 0L),
            ElementKey(ElementType.NODE, 1L)
        )

        assertTrue(dao.getAllForElements(keys).containsExactlyInAnyOrder(listOf(q1, q2, q3)))
    }

    @Test fun getAllInBBox() {
        // in
        val q1 = entry(elementId = 0, pos = p(0.0, 0.0))
        val q2 = entry(elementId = 1, pos = p(1.0, 1.0))
        val q3 = entry(elementId = 2, pos = p(0.5, 0.5))

        dao.putAll(listOf(
            q1, q2, q3,
            // in but wrong quest type
            entry(elementId = 3, questTypeName = "b", pos = p(0.5, 0.5)),
            // out
            entry(elementId = 4, pos = p(-0.5, 0.5)),
            entry(elementId = 5, pos = p(0.5, -0.5)),
            entry(elementId = 6, pos = p(0.5, 1.5)),
            entry(elementId = 7, pos = p(1.5, 0.5)),
        ))

        assertTrue(dao.getAllInBBox(
            BoundingBox(0.0, 0.0, 1.0, 1.0),
            listOf("a")
        ).containsExactlyInAnyOrder(listOf(q1, q2, q3)))
    }

    private fun entry(
        elementType: ElementType = ElementType.NODE,
        elementId: Long = 0L,
        questTypeName: String = "a",
        pos: LatLon = p(0.0, 0.0)
    ) = BasicOsmQuestDaoEntry(elementType, elementId, questTypeName, pos)

    private fun p(x: Double, y: Double): LatLon = LatLon(y, x)
}
