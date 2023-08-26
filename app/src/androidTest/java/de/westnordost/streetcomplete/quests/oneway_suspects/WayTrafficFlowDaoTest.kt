package de.westnordost.streetcomplete.quests.oneway_suspects

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.WayDao
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WayTrafficFlowDaoTest : ApplicationDbTestCase() {

    private lateinit var dao: WayTrafficFlowDao

    @BeforeTest fun createDao() {
        dao = WayTrafficFlowDao(database)
    }

    @Test fun putGetTrue() {
        dao.put(123L, true)
        assertTrue(dao.isForward(123L)!!)
    }

    @Test fun putGetFalse() {
        dao.put(123L, false)
        assertFalse(dao.isForward(123L)!!)
    }

    @Test fun getNull() {
        assertNull(dao.isForward(123L))
    }

    @Test fun delete() {
        dao.put(123L, false)
        dao.delete(123L)
        assertNull(dao.isForward(123L))
    }

    @Test fun overwrite() {
        dao.put(123L, true)
        dao.put(123L, false)
        assertFalse(dao.isForward(123L)!!)
    }

    @Test fun deleteUnreferenced() {
        val wayDao = WayDao(database)

        wayDao.put(Way(1, mutableListOf(), emptyMap(), 0))
        wayDao.put(Way(2, mutableListOf(), emptyMap(), 0))

        dao.put(1, true)
        dao.put(3, true)

        dao.deleteUnreferenced()

        assertTrue(dao.isForward(1)!!)
        assertNull(dao.isForward(3))
    }
}
