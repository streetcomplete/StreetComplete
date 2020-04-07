package de.westnordost.streetcomplete.data.osmnotes.createnotes

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey

import org.junit.Assert.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CreateNoteDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: CreateNoteDao

    @Before fun createDao() {
        dao = CreateNoteDao(dbHelper, CreateNoteMapping(serializer))
    }

    @Test fun getButNothingIsThere() {
        assertNull(dao.get(1L))
    }

    @Test fun getAllButNothingIsThere() {
        assertEquals(listOf<CreateNote>(), dao.getAll())
    }

    @Test fun addGetAndDelete() {
        val listener = mock(CreateNoteDao.Listener::class.java)
        dao.addListener(listener)

        val note = CreateNote(null, "text", OsmLatLon(3.0, 5.0), "title",
                ElementKey(Element.Type.NODE, 132L), arrayListOf("hello", "hey"))

        assertTrue(dao.add(note))
        verify(listener).onAddedCreateNote()
        val dbNote = dao.get(note.id!!)!!

        assertEquals(note, dbNote)

        assertTrue(dao.delete(note.id!!))
        verify(listener).onDeletedCreateNote()

        assertNull(dao.get(note.id!!))
    }

    @Test fun delete() {
        val listener = mock(CreateNoteDao.Listener::class.java)
        dao.addListener(listener)

        val note = CreateNote(null, "text", OsmLatLon(3.0, 5.0))

        assertTrue(dao.add(note))
        verify(listener).onAddedCreateNote()
        assertTrue(dao.delete(note.id!!))
        verify(listener).onDeletedCreateNote()
        assertNull(dao.get(note.id!!))
    }

    @Test fun addAndGetNullableFields() {
        val listener = mock(CreateNoteDao.Listener::class.java)
        dao.addListener(listener)
        val note = CreateNote(null, "text", OsmLatLon(3.0, 5.0))

        assertTrue(dao.add(note))
        verify(listener).onAddedCreateNote()
        val dbNote = dao.get(note.id!!)!!

        assertNull(dbNote.elementKey)
        assertNull(dbNote.questTitle)
        assertNull(dbNote.imagePaths)
    }

    @Test fun getAll() {
        dao.add(CreateNote(null, "this is in", OsmLatLon(0.5, 0.5)))
        dao.add(CreateNote(null, "this is out", OsmLatLon(-0.5, 0.5)))

        assertEquals(1, dao.getAll(BoundingBox(0.0, 0.0, 1.0, 1.0)).size)
        assertEquals(2, dao.getAll().size)
    }

    @Test fun getAllPositions() {
        dao.add(CreateNote(null, "this is in", OsmLatLon(0.5, 0.5)))
        dao.add(CreateNote(null, "this is out", OsmLatLon(-0.5, 0.5)))

        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(OsmLatLon(0.5, 0.5), positions.single())
    }

    @Test fun getCount0() {
        assertEquals(0, dao.getCount())
    }

    @Test fun getCount1() {
        dao.add(CreateNote(null, "joho", OsmLatLon(0.5, 0.5)))
        assertEquals(1, dao.getCount())
    }

    @Test fun getCount2() {
        dao.add(CreateNote(null, "joho", OsmLatLon(0.5, 0.5)))
        dao.add(CreateNote(null, "joho", OsmLatLon(0.1, 0.5)))

        assertEquals(2, dao.getCount())
    }
}
