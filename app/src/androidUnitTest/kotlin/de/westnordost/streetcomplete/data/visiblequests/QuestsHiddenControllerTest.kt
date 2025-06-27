package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestHiddenAt
import de.westnordost.streetcomplete.data.atp.atpquests.AtpQuestsHiddenDao
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHiddenAt
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestHiddenAt
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.AtpQuestKey
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
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
    private lateinit var atpDb: AtpQuestsHiddenDao

    private lateinit var ctrl: QuestsHiddenController

    private lateinit var listener: QuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        osmDb = mock()
        notesDb = mock()
        atpDb = mock()
        listener = mock()
        ctrl = QuestsHiddenController(osmDb, notesDb, atpDb)
        ctrl.addListener(listener)
    }

    @Test fun get() {
        val q1 = osmQuestKey(elementId = 1)
        val q2 = osmQuestKey(elementId = 2)
        val q3 = OsmNoteQuestKey(3)
        val q4 = OsmNoteQuestKey(4)
        val q5 = AtpQuestKey(5)
        val q6 = AtpQuestKey(6)
        on(osmDb.getAll()).thenReturn(listOf(OsmQuestHiddenAt(q1, 123L)))
        on(notesDb.getAll()).thenReturn(listOf(NoteQuestHiddenAt(q3.noteId, 124L)))
        on(notesDb.getTimestamp(q4.noteId)).thenReturn(null)
        on(atpDb.getAll()).thenReturn(listOf(AtpQuestHiddenAt(q5.atpEntryId, 125L)))

        assertEquals(ctrl.get(q1), 123L)
        assertNull(ctrl.get(q2))
        assertEquals(ctrl.get(q3), 124L)
        assertNull(ctrl.get(q4))
        assertEquals(ctrl.get(q5), 125L)
        assertNull(ctrl.get(q6))
    }

    @Test fun getAllNewerThan() {
        val h1 = OsmQuestHiddenAt(osmQuestKey(elementId = 1), 250)
        val h2 = OsmQuestHiddenAt(osmQuestKey(elementId = 2), 123)
        val h3 = NoteQuestHiddenAt(2L, 500)
        val h4 = NoteQuestHiddenAt(3L, 123)
        val h5 = AtpQuestHiddenAt(4L, 23)
        val h6 = AtpQuestHiddenAt(5L, 100000)

        on(osmDb.getAll()).thenReturn(listOf(h1, h2))
        on(notesDb.getAll()).thenReturn(listOf(h3, h4))
        on(atpDb.getAll()).thenReturn(listOf(h5, h6))

        assertEquals<Pair<QuestKey, Long>>(
            Pair(AtpQuestKey(h6.allThePlacesEntryId), 100000),
            ctrl.getAllNewerThan(123L)[0]
        )
        assertEquals<Pair<QuestKey, Long>>(
            Pair(OsmNoteQuestKey(h3.noteId), 500L),
            ctrl.getAllNewerThan(123L)[1]
        )
        assertEquals<Pair<QuestKey, Long>>(
            Pair(h1.key, 250L),
            ctrl.getAllNewerThan(123L)[2]
        )
        //TODO above works, below fails, why? Oh and below fails while claiming no difference
        assertEquals(
            listOf(
                AtpQuestKey(h6.allThePlacesEntryId) to 100000,
                OsmNoteQuestKey(h3.noteId) to 500L,
                h1.key to 250L,
            ),
            ctrl.getAllNewerThan(123L)
        )
    }

    @Test fun countAll() {
        val h1 = OsmQuestHiddenAt(osmQuestKey(elementId = 1), 1)
        val h2 = NoteQuestHiddenAt(1L, 1)
        val h3 = AtpQuestHiddenAt(1L, 1)

        on(osmDb.getAll()).thenReturn(listOf(h1))
        on(notesDb.getAll()).thenReturn(listOf(h2))
        on(atpDb.getAll()).thenReturn(listOf(h3))
        assertEquals(3, ctrl.countAll())
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

    @Test fun `hide AllThePlaces quest`() {
        val q = AtpQuestKey(1)
        on(atpDb.getTimestamp(q.atpEntryId)).thenReturn(123L)

        ctrl.hide(q)

        verify(atpDb).add(q.atpEntryId)
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

    @Test fun `unhide AllThePlaces quest`() {
        val q = AtpQuestKey(2)

        on(atpDb.delete(q.atpEntryId)).thenReturn(true).thenReturn(false)
        on(atpDb.getTimestamp(q.atpEntryId)).thenReturn(123).thenReturn(null)

        assertTrue(ctrl.unhide(q))
        assertFalse(ctrl.unhide(q))

        verify(atpDb, times(2)).getTimestamp(q.atpEntryId)
        verify(atpDb, times(1)).delete(q.atpEntryId)
        verify(listener, times(1)).onUnhid(q, 123)
    }

    @Test fun unhideAll() {
        on(osmDb.deleteAll()).thenReturn(7)
        on(notesDb.deleteAll()).thenReturn(9)
        on(atpDb.deleteAll()).thenReturn(100)

        assertEquals(7 + 9 + 100, ctrl.unhideAll())

        verify(osmDb).deleteAll()
        verify(notesDb).deleteAll()
        verify(atpDb).deleteAll()
        verify(listener).onUnhidAll()
    }
}
