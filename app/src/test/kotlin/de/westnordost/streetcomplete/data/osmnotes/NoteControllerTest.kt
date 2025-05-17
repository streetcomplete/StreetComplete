package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteControllerTest {
    private lateinit var dao: NoteDao
    private lateinit var noteController: NoteController

    @BeforeTest fun setUp() {
        dao = mock()
        noteController = NoteController(dao)
    }

    @Test fun get() {
        val note = note(5)
        on(dao.get(5L)).thenReturn(note)
        assertEquals(note, noteController.get(5L))
    }

    @Test fun `getAll note ids`() {
        val ids = listOf(1L, 2L, 3L)
        val ret = listOf(note(1), note(2), note(3))
        on(dao.getAll(ids)).thenReturn(ret)
        assertEquals(ret, noteController.getAll(ids))
    }

    @Test fun `getAll in bbox`() {
        val bbox = bbox()
        val ret = listOf(note(1), note(2), note(3))
        on(dao.getAll(bbox)).thenReturn(ret)
        assertEquals(ret, noteController.getAll(bbox))
    }

    @Test fun `getAllPositions in bbox`() {
        val bbox = bbox()
        val ret = listOf(p(), p(), p())
        on(dao.getAllPositions(bbox)).thenReturn(ret)
        assertEquals(ret, noteController.getAllPositions(bbox))
    }

    @Test fun put() {
        val note = note(1)

        val listener = mock<NoteController.Listener>()
        noteController.addListener(listener)
        noteController.put(note)

        verify(dao).put(note)

        sleep(100)
        verify(listener).onUpdated(eq(listOf(note)), eq(emptyList()), eq(emptyList()))
    }

    @Test fun `put existing`() {
        val note = note(1)
        val listener = mock<NoteController.Listener>()
        on(dao.get(1L)).thenReturn(note)

        noteController.addListener(listener)
        noteController.put(note)

        verify(dao).put(note)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(listOf(note)), eq(emptyList()))
    }

    @Test fun delete() {
        val listener = mock<NoteController.Listener>()
        on(dao.delete(1L)).thenReturn(true)

        noteController.addListener(listener)
        noteController.delete(1L)
        verify(dao).delete(1L)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(listOf(1L)))
    }

    @Test fun `delete non-existing`() {
        val listener = mock<NoteController.Listener>()
        on(dao.delete(1L)).thenReturn(false)

        noteController.addListener(listener)
        noteController.delete(1L)
        verify(dao).delete(1L)
        verifyNoInteractions(listener)
    }

    @Test fun `remove listener`() {
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)
        noteController.removeListener(listener)
        noteController.put(mock())
        verifyNoInteractions(listener)
    }

    @Test fun deleteOlderThan() {
        val ids = listOf(1L, 2L, 3L)
        on(dao.getIdsOlderThan(123L)).thenReturn(ids)
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)

        assertEquals(3, noteController.deleteOlderThan(123L))
        verify(dao).deleteAll(ids)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(ids))
    }

    @Test fun clear() {
        val listener = mock<NoteController.Listener>()
        noteController.addListener(listener)
        noteController.clear()

        verify(dao).clear()
        verify(listener).onCleared()
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))
        on(dao.getAll(bbox)).thenReturn(emptyList())
        val listener = mock<NoteController.Listener>()

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, notes)
        verify(dao).getAll(bbox)
        verify(dao).putAll(eq(notes))
        verify(dao).deleteAll(eq(emptySet()))

        sleep(100)
        verify(listener).onUpdated(eq(notes), eq(emptyList()), eq(emptySet()))
    }

    @Test fun `putAllForBBox when there is something already`() {
        val note1 = note(1)
        val note2 = note(2)
        val note3 = note(3)
        val bbox = bbox()
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

        sleep(100)
        verify(listener).onUpdated(eq(listOf(note3)), eq(listOf(note1)), eq(setOf(2)))
    }
}
