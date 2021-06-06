package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OsmQuestsHiddenDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmQuestsHiddenDao

    @Before fun createDao() {
        dao = OsmQuestsHiddenDao(database)
    }

    @Test fun getButNothingIsThere() {
        assertFalse(dao.contains(OsmQuestKey(ElementType.NODE, 0L, "bla")))
    }

    @Test fun addAndGet() {
        val key = OsmQuestKey(ElementType.NODE, 123L, "bla")
        dao.add(key)
        assertTrue(dao.contains(key))
    }

    @Test fun addGetDelete() {
        val key = OsmQuestKey(ElementType.NODE, 123L, "bla")
        assertFalse(dao.delete(key))
        dao.add(key)
        assertTrue(dao.delete(key))
        assertFalse(dao.contains(key))
    }

    @Test fun getNewerThan() = runBlocking {
        val keys = listOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        dao.add(keys[0])
        delay(200)
        val time = System.currentTimeMillis()
        dao.add(keys[1])
        val result = dao.getNewerThan(time - 100).single()
        assertEquals(keys[1], result.osmQuestKey)
    }

    @Test fun getAllIds() {
        val keys = listOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertTrue(dao.getAllIds().containsExactlyInAnyOrder(keys))
    }

    @Test fun deleteAll() {
        assertEquals(0, dao.deleteAll())
        val keys = listOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertEquals(2, dao.deleteAll())
        assertFalse(dao.contains(keys[0]))
        assertFalse(dao.contains(keys[1]))
    }
}
