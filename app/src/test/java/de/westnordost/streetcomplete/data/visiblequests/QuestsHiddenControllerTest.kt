package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHiddenAt
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestHiddenAt
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.osmQuestKey
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestsHiddenControllerTest {

    private lateinit var osmDb: OsmQuestsHiddenDao
    private lateinit var notesDb: NoteQuestsHiddenDao

    private lateinit var ctrl: QuestsHiddenController

    private lateinit var listener: QuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        osmDb = mock()
        notesDb = mock()
        listener = mock()
        ctrl = QuestsHiddenController(osmDb, notesDb)
        ctrl.addListener(listener)
    }

    @Test fun get() {
        val q1 = osmQuestKey(elementId = 1)
        val q2 = osmQuestKey(elementId = 2)
        val q3 = OsmNoteQuestKey(3)
        val q4 = OsmNoteQuestKey(4)
        on(osmDb.getAll()).thenReturn(listOf(OsmQuestHiddenAt(q1, 123L)))
        on(notesDb.getAll()).thenReturn(listOf(NoteQuestHiddenAt(q3.noteId, 124L)))
        on(notesDb.getTimestamp(q4.noteId)).thenReturn(null)

        assertEquals(ctrl.get(q1), 123L)
        assertNull(ctrl.get(q2))
        assertEquals(ctrl.get(q3), 124L)
        assertNull(ctrl.get(q4))
    }

    @Test fun getAllNewerThan() {
        val h1 = OsmQuestHiddenAt(osmQuestKey(elementId = 1), 250)
        val h2 = OsmQuestHiddenAt(osmQuestKey(elementId = 2), 123)
        val h3 = NoteQuestHiddenAt(2L, 500)
        val h4 = NoteQuestHiddenAt(3L, 123)

        on(osmDb.getAll()).thenReturn(listOf(h1, h2))
        on(notesDb.getAll()).thenReturn(listOf(h3, h4))

        assertEquals(
            listOf(
                OsmNoteQuestKey(h3.noteId) to 500L,
                h1.key to 250L,
            ),
            ctrl.getAllNewerThan(123L)
        )
    }

    @Test fun countAll() {
        val h1 = OsmQuestHiddenAt(osmQuestKey(elementId = 1), 1)
        val h2 = NoteQuestHiddenAt(1L, 1)

        on(osmDb.getAll()).thenReturn(listOf(h1))
        on(notesDb.getAll()).thenReturn(listOf(h2))
        assertEquals(2, ctrl.countAll())
    }

    @Test fun `hide osm quest`() {
        val q = osmQuestKey(elementId = 1)
        on(osmDb.getTimestamp(q)).thenReturn(123L)

        ctrl.hide(q)

        verify(osmDb).add(q)
        verify(listener).onHid(q, 123)
    }

    @Test fun `hide osm note quest`() {
        val q = OsmNoteQuestKey(1)
        on(notesDb.getTimestamp(q.noteId)).thenReturn(123L)

        ctrl.hide(q)

        verify(notesDb).add(q.noteId)
        verify(listener).onHid(q, 123)
    }

    @Test fun `unhide osm quest`() {
        val q = osmQuestKey()
        on(osmDb.delete(q)).thenReturn(true).thenReturn(false)
        on(osmDb.getTimestamp(q)).thenReturn(123L).thenReturn(null)

        assertTrue(ctrl.unhide(q))
        assertFalse(ctrl.unhide(q))

        verify(osmDb, times(2)).getTimestamp(q)
        verify(osmDb, times(1)).delete(q)
        verify(listener, times(1)).onUnhid(q, 123)
    }

    @Test fun `unhide osm note quest`() {
        val q = OsmNoteQuestKey(2)

        on(notesDb.delete(q.noteId)).thenReturn(true).thenReturn(false)
        on(notesDb.getTimestamp(q.noteId)).thenReturn(123).thenReturn(null)

        assertTrue(ctrl.unhide(q))
        assertFalse(ctrl.unhide(q))

        verify(notesDb, times(2)).getTimestamp(q.noteId)
        verify(notesDb, times(1)).delete(q.noteId)
        verify(listener, times(1)).onUnhid(q, 123)
    }

    @Test fun unhideAll() {
        on(osmDb.deleteAll()).thenReturn(7)
        on(notesDb.deleteAll()).thenReturn(9)

        assertEquals(7 + 9, ctrl.unhideAll())

        verify(osmDb).deleteAll()
        verify(notesDb).deleteAll()
        verify(listener).onUnhidAll()
    }
}
