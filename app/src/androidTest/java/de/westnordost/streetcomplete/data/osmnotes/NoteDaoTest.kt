package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ApplicationDbTestCase
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteDaoTest : ApplicationDbTestCase() {
    private lateinit var dao: NoteDao

    @Before fun createDao() {
        dao = NoteDao(database)
    }

    @Test fun putGetNoClosedDate() {
        val note = createNote()

        dao.put(note)
        val dbNote = dao.get(note.id)!!
        assertEquals(note, dbNote)
    }

    @Test fun putAll() {
        dao.putAll(listOf(createNote(1), createNote(2)))
        assertNotNull(dao.get(1))
        assertNotNull(dao.get(2))
    }

    @Test fun putReplace() {
        var note = createNote()
        dao.put(note)
        note = note.copy(status = Note.Status.CLOSED)
        dao.put(note)

        val dbNote = dao.get(note.id)!!
        assertEquals(note, dbNote)
    }

    @Test fun putGetWithClosedDate() {
        val note = createNote(timestampClosed = 6000)

        dao.put(note)
        val dbNote = dao.get(note.id)!!
        assertEquals(note, dbNote)
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
        val thisIsIn = createNote(1, LatLon(0.5, 0.5))
        val thisIsOut = createNote(2, LatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val positions = dao.getAllPositions(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(LatLon(0.5, 0.5), positions.single())
    }

    @Test fun getAllByBbox() {
        val thisIsIn = createNote(1, LatLon(0.5, 0.5))
        val thisIsOut = createNote(2, LatLon(-0.5, 0.5))
        dao.putAll(listOf(thisIsIn, thisIsOut))

        val notes = dao.getAll(BoundingBox(0.0, 0.0, 1.0, 1.0))
        assertEquals(thisIsIn, notes.single())
    }

    @Test fun getAllByIds() {
        val first = createNote(1)
        val second = createNote(2)
        val third = createNote(3)
        dao.putAll(listOf(first, second, third))

        assertEquals(listOf(first, second), dao.getAll(listOf(1, 2)))
    }

    @Test fun deleteAllByIds() {
        dao.putAll(listOf(createNote(1), createNote(2), createNote(3)))

        assertEquals(2, dao.deleteAll(listOf(1, 2)))
        assertNull(dao.get(1))
        assertNull(dao.get(2))
        assertNotNull(dao.get(3))
    }

    @Test fun getUnusedAndOldIds() {
        dao.putAll(listOf(createNote(1), createNote(2), createNote(3)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10)
        assertTrue(unusedIds.containsExactlyInAnyOrder(listOf(1L, 2L, 3L)))
    }

    @Test fun getUnusedAndOldIdsButAtMostX() {
        dao.putAll(listOf(createNote(1), createNote(2), createNote(3)))
        val unusedIds = dao.getIdsOlderThan(nowAsEpochMilliseconds() + 10, 2)
        assertEquals(2, unusedIds.size)
    }

    @Test fun clear() {
        dao.putAll(listOf(createNote(1), createNote(2), createNote(3)))
        dao.clear()
        assertTrue(dao.getAll(listOf(1L, 2L, 3L)).isEmpty())
    }
}

private fun createNote(
    id: Long = 5,
    position: LatLon = LatLon(1.0, 1.0),
    timestampClosed: Long? = null
): Note {
    val timestampCreated: Long = 5000
    val user = User(5, "PingPong")
    val comment = NoteComment(timestampCreated, NoteComment.Action.OPENED, "hi", user)
    return Note(position, id, timestampCreated, timestampClosed, Note.Status.OPEN, listOf(comment))
}
