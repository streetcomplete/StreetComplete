package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.eq
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NoteControllerTest {
    private lateinit var dao: NoteDao
    private lateinit var noteController: NoteController

    @Before fun setUp() {
        dao = mock()
        noteController = NoteController(dao)
    }

    @Test fun get() {
        val note = note(5)
        on(dao.get(5L)).thenReturn(note)
        assertEquals(note, noteController.get(5L))
    }

    @Test fun `getAll note ids`() {
        val ids = listOf(1L,2L,3L)
        val ret = listOf(note(1), note(2), note(3))
        on(dao.getAll(ids)).thenReturn(ret)
        assertEquals(ret, noteController.getAll(ids))
    }

    @Test fun `getAll in bbox`() {
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val ret = listOf(note(1), note(2), note(3))
        on(dao.getAll(bbox)).thenReturn(ret)
        assertEquals(ret, noteController.getAll(bbox))
    }

    @Test fun `getAllPositions in bbox`() {
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val ret = listOf<LatLon>(mock(), mock(), mock())
        on(dao.getAllPositions(bbox)).thenReturn(ret)
        assertEquals(ret, noteController.getAllPositions(bbox))
    }

    @Test fun put() {
        val note = note(1)

        val listener = mock<NoteController.Listener>()
        noteController.addListener(listener)
        noteController.put(note)

        verify(dao).put(note)
        verify(listener).onUpdated(eq(listOf(note)), eq(emptyList()), eq(emptyList()))
    }

    @Test fun `put existing`() {
        val note = note(1)
        val listener = mock<NoteController.Listener>()
        on(dao.get(1L)).thenReturn(note)

        noteController.addListener(listener)
        noteController.put(note)

        verify(dao).put(note)
        verify(listener).onUpdated(eq(emptyList()), eq(listOf(note)), eq(emptyList()))
    }

    @Test fun delete() {
        val listener = mock<NoteController.Listener>()
        on(dao.delete(1L)).thenReturn(true)

        noteController.addListener(listener)
        noteController.delete(1L)
        verify(dao).delete(1L)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(listOf(1L)))
    }

    @Test fun `delete non-existing`() {
        val listener = mock<NoteController.Listener>()
        on(dao.delete(1L)).thenReturn(false)

        noteController.addListener(listener)
        noteController.delete(1L)
        verify(dao).delete(1L)
        verifyZeroInteractions(listener)
    }

    @Test fun `remove listener`() {
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)
        noteController.removeListener(listener)
        noteController.put(mock())
        verifyZeroInteractions(listener)
    }

    @Test fun deleteAllOlderThan() {
        val ids = listOf(1L,2L,3L)
        on(dao.getAllIdsOlderThan(123L)).thenReturn(ids)
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)

        assertEquals(3, noteController.deleteAllOlderThan(123L))
        verify(dao).deleteAll(ids)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(ids))
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val notes = listOf(note(1), note(2), note(3))
        on(dao.getAll(bbox)).thenReturn(emptyList())
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, notes)
        verify(dao).getAll(bbox)
        verify(dao).putAll(eq(notes))
        verify(dao).deleteAll(eq(emptySet()))
        verify(listener).onUpdated(eq(notes), eq(emptyList()), eq(emptySet()))
    }

    @Test fun `putAllForBBox when there is something already`() {
        val note1 = note(1)
        val note2 = note(2)
        val note3 = note(3)
        val bbox = BoundingBox(0.0,1.0,2.0,3.0)
        val oldNotes = listOf(note1, note2)
        // 1 is updated, 2 is deleted, 3 is added
        val newNotes = listOf(note1, note3)
        on(dao.getAll(bbox)).thenReturn(oldNotes)
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, newNotes)
        verify(dao).getAll(bbox)
        verify(dao).putAll(eq(newNotes))
        verify(dao).deleteAll(eq(setOf(2L)))
        verify(listener).onUpdated(eq(listOf(note3)), eq(listOf(note1)), eq(setOf(2)))
    }
}

private fun note(id: Long) = Note().apply { this.id = id }
