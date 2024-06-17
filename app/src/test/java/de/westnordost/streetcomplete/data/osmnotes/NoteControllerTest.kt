package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteControllerTest {
    @Mock private lateinit var dao: NoteDao
    @Mock private lateinit var listener: NoteController.Listener
    private lateinit var noteController: NoteController

    @BeforeTest fun setUp() {
        dao = mock(classOf<NoteDao>())
        noteController = NoteController(dao)
    }

    @Test fun get() {
        val note = note(5)
        every { dao.get(5L) }.returns(note)
        assertEquals(note, noteController.get(5L))
    }

    @Test fun `getAll note ids`() {
        val ids = listOf(1L, 2L, 3L)
        val ret = listOf(note(1), note(2), note(3))
        every { dao.getAll(ids) }.returns(ret)
        assertEquals(ret, noteController.getAll(ids))
    }

    @Test fun `getAll in bbox`() {
        val bbox = bbox()
        val ret = listOf(note(1), note(2), note(3))
        every { dao.getAll(bbox) }.returns(ret)
        assertEquals(ret, noteController.getAll(bbox))
    }

    @Test fun `getAllPositions in bbox`() {
        val bbox = bbox()
        val ret = listOf(p(), p(), p())
        every { dao.getAllPositions(bbox) }.returns(ret)
        assertEquals(ret, noteController.getAllPositions(bbox))
    }

    @Test fun put() {
        val note = note(1)
        every { dao.get(1L) }.returns(null)
        val listener = mock(classOf<NoteController.Listener>())
        noteController.addListener(listener)
        noteController.put(note)

        verifyInvokedExactlyOnce { dao.put(note) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(listOf(note), emptyList(), emptyList()) }
    }

    @Test fun `put existing`() {
        val note = note(1)
        val listener = mock(classOf<NoteController.Listener>())
        every { dao.get(1L) }.returns(note)

        noteController.addListener(listener)
        noteController.put(note)

        verifyInvokedExactlyOnce { dao.put(note) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(emptyList(), listOf(note), emptyList()) }
    }

    @Test fun delete() {
        val listener = mock(classOf<NoteController.Listener>())
        every { dao.delete(1L) }.returns(true)

        noteController.addListener(listener)
        noteController.delete(1L)
        verifyInvokedExactlyOnce { dao.delete(1L) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(emptyList(), emptyList(), listOf(1L)) }
    }

    @Test fun `delete non-existing`() {
        val listener = mock(classOf<NoteController.Listener>())
        every { dao.delete(1L) }.returns(false)

        noteController.addListener(listener)
        noteController.delete(1L)
        verifyInvokedExactlyOnce { dao.delete(1L) }
    }

    @Test fun `remove listener`() {
        val listener = mock(classOf<NoteController.Listener>())
        every { dao.get(0L) }.returns(note(0L))
        noteController.addListener(listener)
        noteController.removeListener(listener)
        noteController.put(note(0))
    }

    @Test fun deleteOlderThan() {
        val ids = listOf(1L, 2L, 3L)
        every { dao.getIdsOlderThan(123L) }.returns(ids)
        every { dao.deleteAll(ids) }.returns(3)
        val listener = mock(classOf<NoteController.Listener>())

        noteController.addListener(listener)

        assertEquals(3, noteController.deleteOlderThan(123L))
        verifyInvokedExactlyOnce { dao.deleteAll(ids) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(emptyList(), emptyList(), ids) }
    }

    @Test fun clear() {
        val listener = mock(classOf<NoteController.Listener>())
        noteController.addListener(listener)
        noteController.clear()

        verifyInvokedExactlyOnce { dao.clear() }
        verifyInvokedExactlyOnce { listener.onCleared() }
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))
        every { dao.getAll(bbox) }.returns(emptyList())
        every { dao.deleteAll(emptySet()) }.returns(0)
        val listener = mock(classOf<NoteController.Listener>())

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, notes)
        verifyInvokedExactlyOnce { dao.getAll(bbox) }
        verifyInvokedExactlyOnce { dao.putAll(notes) }
        verifyInvokedExactlyOnce { dao.deleteAll(emptySet()) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(notes, emptyList(), emptySet()) }
    }

    @Test fun `putAllForBBox when there is something already`() {
        val note1 = note(1)
        val note2 = note(2)
        val note3 = note(3)
        val bbox = bbox()
        val oldNotes = listOf(note1, note2)
        // 1 is updated, 2 is deleted, 3 is added
        val newNotes = listOf(note1, note3)
        every { dao.getAll(bbox) }.returns(oldNotes)
        every { dao.deleteAll(setOf(2L)) }.returns(1)
        val listener = mock(classOf<NoteController.Listener>())

        noteController.addListener(listener)
        noteController.putAllForBBox(bbox, newNotes)
        verifyInvokedExactlyOnce { dao.getAll(bbox) }
        verifyInvokedExactlyOnce { dao.putAll(newNotes) }
        verifyInvokedExactlyOnce { dao.deleteAll(setOf(2L)) }

        sleep(100)
        verifyInvokedExactlyOnce { listener.onUpdated(listOf(note3), listOf(note1), setOf(2)) }
    }
}
