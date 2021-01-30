package de.westnordost.streetcomplete.data.osmnotes.commentnotes

import org.junit.Before
import org.junit.Test

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon

import org.junit.Assert.*
import org.mockito.Mockito.*

class CommentNoteDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: CommentNoteDao

    @Before fun createDao() {
        dao = CommentNoteDao(dbHelper, CommentNoteMapping(serializer))
    }

    @Test fun getButNothingIsThere() {
        assertNull(dao.get(1L))
    }

    @Test fun getAllButNothingIsThere() {
        assertEquals(listOf<CommentNote>(), dao.getAll())
    }

    @Test fun addGetAndDelete() {
        val listener = mock(CommentNoteDao.Listener::class.java)
        dao.addListener(listener)

        val n = note()
        assertTrue(dao.add(n))
        verify(listener).onAddedCommentNote(n)
        assertEquals(n, dao.get(n.noteId))

        assertTrue(dao.delete(n.noteId))
        verify(listener).onDeletedCommentNote(n.noteId)

        assertNull(dao.get(n.noteId))
    }

    @Test fun delete() {
        val listener = mock(CommentNoteDao.Listener::class.java)
        dao.addListener(listener)

        val n = note(1)
        assertTrue(dao.add(note(1)))
        verify(listener).onAddedCommentNote(n)
        assertTrue(dao.delete(1))
        verify(listener).onDeletedCommentNote(n.noteId)
        assertNull(dao.get(1))
    }

    @Test fun addAndGetNullableFields() {
        val listener = mock(CommentNoteDao.Listener::class.java)
        dao.addListener(listener)

        val n = note(imagePaths = null)
        assertTrue(dao.add(n))
        verify(listener).onAddedCommentNote(n)
        val dbNote = dao.get(n.noteId)!!

        assertNull(dbNote.imagePaths)
    }

    @Test fun getAll() {
        dao.add(note(0))
        dao.add(note(1))

        assertEquals(2, dao.getAll().size)
    }

    @Test fun getAllPositions() {
        dao.add(note(0, OsmLatLon(0.5, 0.5), "this is in"))
        dao.add(note(1, OsmLatLon(-0.5, 0.5), "this is out"))

        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(OsmLatLon(0.5, 0.5), positions.single())
    }

    @Test fun getCount0() {
        assertEquals(0, dao.getCount())
    }

    @Test fun getCount1() {
        dao.add(note())
        assertEquals(1, dao.getCount())
    }

    @Test fun getCount2() {
        dao.add(note(0))
        dao.add(note(1))
        dao.add(note(1)) // adding same does not increase count

        assertEquals(2, dao.getCount())
    }
}

private fun note(
    noteId: Long = 123L,
    position: LatLon = OsmLatLon(1.5, 0.5),
    text: String = "bla",
    imagePaths: List<String>? = listOf("a", "b")
) = CommentNote(noteId, position, text, imagePaths)
