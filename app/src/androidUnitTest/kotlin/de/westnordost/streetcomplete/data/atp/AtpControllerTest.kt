package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.testutils.atpEntry
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AtpControllerTest {
    private lateinit var dao: AtpDao
    private lateinit var atpController: AtpController

    @BeforeTest fun setUp() {
        dao = mock()
        atpController = AtpController(dao)
    }

    @Test fun get() {
        val atpEntry = atpEntry(5)
        on(dao.get(5L)).thenReturn(atpEntry)
        assertEquals(atpEntry, atpController.get(5L))
    }

    @Test fun `getAll entry ids`() {
        val ids = listOf(1L, 2L, 3L)
        val ret = listOf(atpEntry(1), atpEntry(2), atpEntry(3))
        on(dao.getAll(ids)).thenReturn(ret)
        assertEquals(ret, atpController.getAll(ids))
    }

    @Test fun `getAll in bbox`() {
        val bbox = bbox()
        val ret = listOf(atpEntry(1), atpEntry(2), atpEntry(3))
        on(dao.getAll(bbox)).thenReturn(ret)
        assertEquals(ret, atpController.getAll(bbox))
    }

    @Test fun `getAllPositions in bbox`() {
        val bbox = bbox()
        val ret = listOf(p(), p(), p())
        on(dao.getAllPositions(bbox)).thenReturn(ret)
        assertEquals(ret, atpController.getAllPositions(bbox))
    }

    @Test fun put() {
        val atpEntry = atpEntry(1)

        val listener = mock<AtpController.Listener>()
        atpController.addListener(listener)
        atpController.put(atpEntry)

        verify(dao).put(atpEntry)

        sleep(100)
        verify(listener).onUpdated(eq(listOf(atpEntry)), eq(emptyList()), eq(emptyList()))
    }

    @Test fun `put existing`() {
        val atpEntry = atpEntry(1)
        val listener = mock<AtpController.Listener>()
        on(dao.get(1L)).thenReturn(atpEntry)

        atpController.addListener(listener)
        atpController.put(atpEntry)

        verify(dao).put(atpEntry)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(listOf(atpEntry)), eq(emptyList()))
    }

    @Test fun delete() {
        val listener = mock<AtpController.Listener>()
        on(dao.delete(1L)).thenReturn(true)

        atpController.addListener(listener)
        atpController.delete(1L)
        verify(dao).delete(1L)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(listOf(1L)))
    }

    @Test fun `delete non-existing`() {
        val listener = mock<AtpController.Listener>()
        on(dao.delete(1L)).thenReturn(false)

        atpController.addListener(listener)
        atpController.delete(1L)
        verify(dao).delete(1L)
        verifyNoInteractions(listener)
    }

    @Test fun `remove listener`() {
        val listener = mock<AtpController.Listener>()

        atpController.addListener(listener)
        atpController.removeListener(listener)
        atpController.put(mock())
        verifyNoInteractions(listener)
    }

    @Test fun deleteOlderThan() {
        val ids = listOf(1L, 2L, 3L)
        on(dao.getIdsOlderThan(123L)).thenReturn(ids)
        val listener = mock<AtpController.Listener>()

        atpController.addListener(listener)

        assertEquals(3, atpController.deleteOlderThan(123L))
        verify(dao).deleteAll(ids)

        sleep(100)
        verify(listener).onUpdated(eq(emptyList()), eq(emptyList()), eq(ids))
    }

    @Test fun clear() {
        val listener = mock<AtpController.Listener>()
        atpController.addListener(listener)
        atpController.clear()

        verify(dao).clear()
        verify(listener).onCleared()
    }

    @Test fun `putAllForBBox when nothing was there before`() {
        val bbox = bbox()
        val atpEntries = listOf(atpEntry(1), atpEntry(2), atpEntry(3))
        on(dao.getAll(bbox)).thenReturn(emptyList())
        val listener = mock<AtpController.Listener>()

        atpController.addListener(listener)
        atpController.putAllForBBox(bbox, atpEntries)
        verify(dao).getAll(bbox)
        verify(dao).putAll(eq(atpEntries))
        verify(dao).deleteAll(eq(emptySet()))

        sleep(100)
        verify(listener).onUpdated(eq(atpEntries), eq(emptyList()), eq(emptySet()))
    }

    @Test fun `putAllForBBox when there is something already`() {
        val atpEntry1 = atpEntry(1)
        val atpEntry2 = atpEntry(2)
        val atpEntry3 = atpEntry(3)
        val bbox = bbox()
        val oldNotes = listOf(atpEntry1, atpEntry2)
        // 1 is updated, 2 is deleted, 3 is added
        val newNotes = listOf(atpEntry1, atpEntry3)
        on(dao.getAll(bbox)).thenReturn(oldNotes)
        val listener = mock<AtpController.Listener>()

        atpController.addListener(listener)
        atpController.putAllForBBox(bbox, newNotes)
        verify(dao).getAll(bbox)
        verify(dao).putAll(eq(newNotes))
        verify(dao).deleteAll(eq(setOf(2L)))

        sleep(100)
        verify(listener).onUpdated(eq(listOf(atpEntry3)), eq(listOf(atpEntry1)), eq(setOf(2)))
    }
}
