package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.persist.WayDao
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.util.Serializer

class WayTrafficFlowSegmentsDaoTest : ApplicationDbTestCase() {

    private lateinit var dao: WayTrafficFlowDao

    override fun setUp() {
        super.setUp()
        dao = WayTrafficFlowDao(dbHelper)
    }

    fun testPutGetTrue() {
        dao.put(123L, true)
        assertTrue(dao.isForward(123L)!!)
    }

    fun testPutGetFalse() {
        dao.put(123L, false)
        assertFalse(dao.isForward(123L)!!)
    }

    fun testGetNull() {
        assertNull(dao.isForward(123L))
    }

    fun testDelete() {
        dao.put(123L, false)
        dao.delete(123L)
        assertNull(dao.isForward(123L))
    }

    fun testOverwrite() {
        dao.put(123L, true)
        dao.put(123L, false)
        assertFalse(dao.isForward(123L)!!)
    }

    fun testDeleteUnreferenced() {
        val mockSerializer = object : Serializer {
            override fun toBytes(`object`: Any) = ByteArray(0)
            override fun <T> toObject(bytes: ByteArray, type: Class<T>) = type.newInstance()
        }
        val wayDao = WayDao(dbHelper, mockSerializer)

        wayDao.put(OsmWay(1, 0, mutableListOf(), null))
        wayDao.put(OsmWay(2, 0, mutableListOf(), null))

        dao.put(1, true)
        dao.put(3, true)

        dao.deleteUnreferenced()

        assertTrue(dao.isForward(1)!!)
        assertNull(dao.isForward(3))
    }
}
