package de.westnordost.streetcomplete.data.osm.persist

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way

import org.junit.Assert.assertEquals


class WayDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: WayDao

    @Before fun createDao() {
        dao = WayDao(dbHelper, WayMapping(serializer))
    }

    @Test fun putGetNoTags() {
        val way = OsmWay(5, 1, listOf(1L, 2L, 3L, 4L), null)
        dao.put(way)
        val dbWay = dao.get(5)

        checkEqual(way, dbWay!!)
    }

    @Test fun putGetWithTags() {
        val way = OsmWay(5, 1, listOf(1L, 2L, 3L, 4L), mapOf("a key" to "a value"))
        dao.put(way)
        val dbWay = dao.get(5)

        checkEqual(way, dbWay!!)
    }

    private fun checkEqual(way: Way, dbWay: Way) {
        assertEquals(way.id, dbWay.id)
        assertEquals(way.version, dbWay.version)
        assertEquals(way.nodeIds, dbWay.nodeIds)
        assertEquals(way.tags, dbWay.tags)
    }
}
