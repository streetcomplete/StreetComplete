package de.westnordost.streetcomplete.quests.oneway_suspects

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.WayDao
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.util.KryoSerializer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class WayTrafficFlowDaoTest : ApplicationDbTestCase() {

    private lateinit var dao: WayTrafficFlowDao

    @Before fun createDao() {
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
        val wayDao = WayDao(database, KryoSerializer())

        wayDao.put(OsmWay(1, 0, mutableListOf(), null, null, Date()))
        wayDao.put(OsmWay(2, 0, mutableListOf(), null, null, Date()))

        dao.put(1, true)
        dao.put(3, true)

        dao.deleteUnreferenced()

        assertTrue(dao.isForward(1)!!)
        assertNull(dao.isForward(3))
    }
}
