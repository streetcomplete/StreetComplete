package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenChangesetsDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: OpenChangesetsDao

    private val Q = "Hurzipurz"
    private val P = "Brasliweks"
    private val SOURCE = "test"

    @BeforeTest fun createDao() {
        dao = OpenChangesetsDao(database)
    }

    @Test fun deleteNonExistent() {
        assertFalse(dao.delete(Q, SOURCE))
    }

    @Test fun createDelete() {
        dao.put(OpenChangeset(Q, SOURCE, 1, LatLon(0.0, 0.0)))
        assertTrue(dao.delete(Q, SOURCE))
        assertNull(dao.get(Q, SOURCE))
    }

    @Test fun getNull() {
        assertNull(dao.get(Q, SOURCE))
    }

    @Test fun insertChangesetId() {
        dao.put(OpenChangeset(Q, SOURCE, 12, LatLon(0.0, 0.0)))
        val info = dao.get(Q, SOURCE)!!
        assertEquals(12, info.changesetId)
        assertEquals(Q, info.questType)
        assertEquals(SOURCE, info.source)
    }

    @Test fun replaceChangesetId() {
        dao.put(OpenChangeset(Q, SOURCE, 12, LatLon(0.0, 0.0)))
        dao.put(OpenChangeset(Q, SOURCE, 6497, LatLon(0.0, 0.0)))
        assertEquals(6497, dao.get(Q, SOURCE)!!.changesetId)
    }

    @Test fun replaceLastPosition() {
        dao.put(OpenChangeset(Q, SOURCE, 12, LatLon(1.0, 2.0)))
        assertEquals(LatLon(1.0, 2.0), dao.get(Q, SOURCE)!!.lastPosition)
        dao.put(OpenChangeset(Q, SOURCE, 12, LatLon(23.0, 12.0)))
        assertEquals(LatLon(23.0, 12.0), dao.get(Q, SOURCE)!!.lastPosition)
    }

    @Test fun getNone() {
        assertTrue(dao.getAll().isEmpty())
    }

    @Test fun insertTwo() {
        dao.put(OpenChangeset(Q, SOURCE, 1, LatLon(0.0, 0.0)))
        dao.put(OpenChangeset(P, SOURCE, 2, LatLon(0.0, 0.0)))
        assertEquals(2, dao.getAll().size)
    }
}
