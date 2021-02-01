package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OsmQuestsHiddenDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmQuestsHiddenDao

    @Before fun createDao() {
        dao = OsmQuestsHiddenDao(dbHelper, OsmQuestsHiddenMapping())
    }

    @Test fun getButNothingIsThere() {
        assertFalse(dao.contains(OsmQuestKey(Element.Type.NODE, 0L, "bla")))
    }

    @Test fun addAndGet() {
        val key = OsmQuestKey(Element.Type.NODE, 123L, "bla")
        dao.add(key)
        assertTrue(dao.contains(key))
    }

    @Test fun getAll() {
        val keys = listOf(
            OsmQuestKey(Element.Type.NODE, 123L, "bla"),
            OsmQuestKey(Element.Type.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertTrue(dao.getAll().containsExactlyInAnyOrder(keys))
    }

    @Test fun deleteAll() {
        assertEquals(0, dao.deleteAll())
        val keys = listOf(
            OsmQuestKey(Element.Type.NODE, 123L, "bla"),
            OsmQuestKey(Element.Type.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertEquals(2, dao.deleteAll())
        assertFalse(dao.contains(keys[1]))
        assertFalse(dao.contains(keys[2]))
    }
}
