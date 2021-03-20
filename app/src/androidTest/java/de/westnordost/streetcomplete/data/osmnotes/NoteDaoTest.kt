package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import org.junit.Before
import org.junit.Test

import java.util.Date

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.osmapi.user.User

import org.junit.Assert.*

class NoteDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NoteDao

    @Before fun createDao() {
        dao = NoteDao(database, serializer)
    }

    @Test fun putGetNoClosedDate() {
        val note = createNote()

        dao.put(note)
        val dbNote = dao.get(note.id)!!
        checkEqual(note, dbNote)
    }

    @Test fun putAll() {
        dao.putAll(listOf(createNote(1), createNote(2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun putReplace() {
        val note = createNote()
        dao.put(note)
        note.status = Note.Status.CLOSED
        dao.put(note)

        val dbNote = dao.get(note.id)!!
        checkEqual(note, dbNote)
    }

    @Test fun putGetWithClosedDate() {
        val note = createNote()
        note.dateClosed = Date(6000)

        dao.put(note)
        val dbNote = dao.get(note.id)!!
        checkEqual(note, dbNote)
    }

    @Test fun deleteButNothingIsThere() {
        assertFalse(dao.delete(1))
    }

    @Test fun delete() {
        val note = createNote()
        dao.put(note)
        assertTrue(dao.delete(note.id))
        assertNull(dao.get(note.id))
    }

    @Test fun getAllPositions() {
        val thisIsIn = createNote(1, OsmLatLon(0.5, 0.5))
        val thisIsOut = createNote(2, OsmLatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(OsmLatLon(0.5, 0.5), positions.single())
    }

    @Test fun getAllByBbox() {
        val thisIsIn = createNote(1, OsmLatLon(0.5, 0.5))
        val thisIsOut = createNote(2, OsmLatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val notes = dao.getAll(BoundingBox(0.0, 0.0, 1.0, 1.0))
        checkEqual(thisIsIn, notes.single())
    }

    @Test fun getAllByIds() {
        val first = createNote(1)
        val second = createNote(2)
        val third = createNote(3)
        dao.putAll(listOf(first, second, third))

        val notes = dao.getAll(listOf(1,2))
        assertEquals(2, notes.size)
        checkEqual(first, notes[0])
        checkEqual(second, notes[1])
    }

    @Test fun deleteAllByIds() {
        dao.putAll(listOf(createNote(1), createNote(2), createNote(3)))

        assertEquals(2, dao.deleteAll(listOf(1,2)))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
        assertNotNull(dao.get(3))
    }
}

private fun checkEqual(note: Note, dbNote: Note) {
    assertEquals(note.id, dbNote.id)
    assertEquals(note.position, dbNote.position)
    assertEquals(note.status, dbNote.status)
    assertEquals(note.dateCreated, dbNote.dateCreated)
    assertEquals(note.dateClosed, dbNote.dateClosed)

    assertEquals(note.comments.size, dbNote.comments.size)
    val it: ListIterator<NoteComment>
    val dbIt: ListIterator<NoteComment>
    it = note.comments.listIterator()
    dbIt = dbNote.comments.listIterator()

    while (it.hasNext() && dbIt.hasNext()) {
        val comment = it.next()
        val dbComment = dbIt.next()
        assertEquals(comment.action, dbComment.action)
        assertEquals(comment.date, dbComment.date)
        assertEquals(comment.text, dbComment.text)
        assertEquals(comment.user.displayName, dbComment.user.displayName)
        assertEquals(comment.user.id, dbComment.user.id)
    }
}

private fun createNote(id: Long = 5, position: LatLon = OsmLatLon(1.0, 1.0)): Note {
    val note = Note()
    note.position = position
    note.status = Note.Status.OPEN
    note.id = id
    note.dateCreated = Date(5000)

    val comment = NoteComment()
    comment.text = "hi"
    comment.date = Date(5000)
    comment.action = NoteComment.Action.OPENED
    comment.user = User(5, "PingPong")
    note.comments.add(comment)

    return note
}
