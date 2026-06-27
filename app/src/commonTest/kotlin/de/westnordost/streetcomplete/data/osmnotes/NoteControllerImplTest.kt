package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.p
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteControllerImplTest {
    private lateinit var dao: NoteDao
    private lateinit var noteController: NoteController

    @BeforeTest fun setUp() {
        dao = mock()
        noteController = NoteControllerImpl(dao)
    }

    @Test fun get() {
        val note = note(5)
        every { dao.get(5L) } returns note
        assertEquals(note, noteController.get(5L))
    }

    @Test fun `getAll note ids`() {
        val ids = listOf(1L, 2L, 3L)
        val ret = listOf(note(1), note(2), note(3))
        every { dao.getAll(ids) } returns ret
        assertEquals(ret, noteController.getAll(ids))
    }

    @Test fun `getAll in bbox`() {
        val bbox = bbox()
        val ret = listOf(note(1), note(2), note(3))
        every { dao.getAll(bbox) } returns ret
        assertEquals(ret, noteController.getAll(bbox))
    }

    @Test fun `getAllPositions in bbox`() {
        val bbox = bbox()
        val ret = listOf(p(), p(), p())
        every { dao.getAllPositions(bbox) } returns ret
        assertEquals(ret, noteController.getAllPositions(bbox))
    }

    @Test fun put(): Unit = runBlocking {
        val note = note(1)

        val listener = mock<NoteSource.Listener>()
        noteController.addListener(listener)
        noteController.put(note)

        verify { dao.put(note) }

        delay(100)

        verify { listener.onUpdated(listOf(note), emptyList(), emptyList()) }
    }

    @Test fun `put existing`(): Unit = runBlocking {
        val note = note(1)
        val listener = mock<NoteSource.Listener>()
        every { dao.get(1L) } returns note

        noteController.addListener(listener)
        noteController.put(note)

        verify { dao.put(note) }

        delay(100)

        verify { listener.onUpdated(emptyList(), listOf(note), emptyList()) }
    }

    @Test fun delete(): Unit = runBlocking {
        val listener = mock<NoteSource.Listener>()
        every { dao.delete(1L) } returns true

        noteController.addListener(listener)
        noteController.delete(1L)
        verify { dao.delete(1L) }

        delay(100)

        verify { listener.onUpdated(emptyList(), emptyList(), listOf(1L)) }
    }

    @Test fun `delete non-existing`() {
        val listener = mock<NoteSource.Listener>()
        every { dao.delete(1L) } returns false

        noteController.addListener(listener)
        noteController.delete(1L)
        verify { dao.delete(1L) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `remove listener`() {
        val listener = mock<NoteSource.Listener>()

        noteController.addListener(listener)
        noteController.removeListener(listener)
        noteController.put(note())
        verifyNoMoreCalls(listener)
    }

    @Test fun deleteOlderThan(): Unit = runBlocking {
        val ids = listOf(1L, 2L, 3L)
        every { dao.getIdsOlderThan(123L) } returns ids
        val listener = mock<NoteSource.Listener>()

        noteController.addListener(listener)

        assertEquals(3, noteController.deleteOlderThan(123L))
        verify { dao.deleteAll(ids) }

        delay(100)

        verify { listener.onUpdated(emptyList(), emptyList(), ids) }
    }

    @Test fun clear() {
        val listener = mock<NoteSource.Listener>()
        noteController.addListener(listener)
        noteController.clear()

        verify { dao.clear() }
        verify { listener.onCleared() }
    }

    @Test fun `putAllForBBox when nothing was there before`(): Unit = runBlocking {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))
        every { dao.getAll(bbox) } returns emptyList()
        val listener = mock<NoteSource.Listener>()

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, notes)
        verify { dao.getAll(bbox) }
        verify { dao.putAll(notes) }
        verify { dao.deleteAll(emptySet()) }

        delay(100)

        verify { listener.onUpdated(notes, emptyList(), emptySet()) }
    }

    @Test fun `putAllForBBox when there is something already`(): Unit = runBlocking {
        val note1 = note(1)
        val note2 = note(2)
        val note3 = note(3)
        val bbox = bbox()
        val oldNotes = listOf(note1, note2)
        // 1 is updated, 2 is deleted, 3 is added
        val newNotes = listOf(note1, note3)
        every { dao.getAll(bbox) } returns oldNotes
        val listener = mock<NoteSource.Listener>()

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, newNotes)
        verify { dao.getAll(bbox) }
        verify { dao.putAll(newNotes) }
        verify { dao.deleteAll(setOf(2L)) }

        delay(100)

        verify { listener.onUpdated(listOf(note3), listOf(note1), setOf(2)) }
    }
}
