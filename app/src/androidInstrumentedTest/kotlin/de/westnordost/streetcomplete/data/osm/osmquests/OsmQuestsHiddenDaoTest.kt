package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsmQuestsHiddenDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OsmQuestsHiddenDao

    @BeforeTest fun createDao() {
        dao = OsmQuestsHiddenDao(database)
    }

    @Test fun addGetDelete() {
        val key = OsmQuestKey(ElementType.NODE, 123L, "bla")
        assertFalse(dao.delete(key))
        dao.add(key)
        assertNotNull(dao.getTimestamp(key))
        assertTrue(dao.delete(key))
        assertNull(dao.getTimestamp(key))
    }

    @Test fun getNewerThan() = runBlocking {
        val keys = listOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        dao.add(keys[0])
        delay(200)
        val time = nowAsEpochMilliseconds()
        dao.add(keys[1])
        val result = dao.getNewerThan(time - 100).single()
        assertEquals(keys[1], result.key)
    }

    @Test fun getAll() {
        val keys = setOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertEquals(
            keys,
            dao.getAll().map { it.key }.toSet()
        )
    }

    @Test fun deleteAll() {
        assertEquals(0, dao.deleteAll())
        val keys = listOf(
            OsmQuestKey(ElementType.NODE, 123L, "bla"),
            OsmQuestKey(ElementType.NODE, 124L, "bla")
        )
        keys.forEach { dao.add(it) }
        assertEquals(2, dao.deleteAll())
        assertNull(dao.getTimestamp(keys[0]))
        assertNull(dao.getTimestamp(keys[1]))
    }

    @Test fun countAll() {
        assertEquals(0, dao.countAll())
        dao.add(OsmQuestKey(ElementType.NODE, 123L, "bla"))
        assertEquals(1, dao.countAll())
    }
}
