package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.persist.WayDao
import de.westnordost.streetcomplete.data.osm.persist.WayMapping
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.util.KryoSerializer
import de.westnordost.streetcomplete.util.Serializer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WayTrafficFlowSegmentsDaoTest : ApplicationDbTestCase() {

    private lateinit var dao: WayTrafficFlowDao

    @Before fun createDao() {
        dao = WayTrafficFlowDao(dbHelper)
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
        val wayDao = WayDao(dbHelper, WayMapping(KryoSerializer()))

        wayDao.put(OsmWay(1, 0, mutableListOf(), null))
        wayDao.put(OsmWay(2, 0, mutableListOf(), null))

        dao.put(1, true)
        dao.put(3, true)

        dao.deleteUnreferenced()

        assertTrue(dao.isForward(1)!!)
        assertNull(dao.isForward(3))
    }
}
