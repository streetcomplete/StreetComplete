package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHiddenAt
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestHiddenAt
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.testutils.QUEST_TYPE
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HiddenQuestsControllerTest {

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
        val q1 = OsmQuestKey(NODE, 1, QUEST_TYPE.name)
        val q2 = OsmQuestKey(NODE, 2, QUEST_TYPE.name)
        val q3 = OsmNoteQuestKey(3)
        val q4 = OsmNoteQuestKey(4)
        on(osmDb.getTimestamp(q1)).thenReturn(123)
        on(osmDb.getTimestamp(q2)).thenReturn(null)
        on(notesDb.getTimestamp(q3.noteId)).thenReturn(123)
        on(notesDb.getTimestamp(q4.noteId)).thenReturn(null)

        assertNotNull(ctrl.get(q1))
        assertNull(ctrl.get(q2))
        assertNotNull(ctrl.get(q3))
        assertNull(ctrl.get(q4))
    }

    @Test fun getAllNewerThan() {
        val osmQuestHiddenAt = OsmQuestHiddenAt(OsmQuestKey(NODE, 1L, QUEST_TYPE.name), 250)
        val noteQuestHiddenAt = NoteQuestHiddenAt(2L, 500)

        on(osmDb.getNewerThan(123L)).thenReturn(listOf(osmQuestHiddenAt))
        on(notesDb.getNewerThan(123L)).thenReturn(listOf(noteQuestHiddenAt))

        assertEquals(
            listOf(
                OsmNoteQuestKey(noteQuestHiddenAt.noteId) to 500L,
                osmQuestHiddenAt.osmQuestKey to 250L,
            ),
            ctrl.getAllNewerThan(123L)
        )
    }

    @Test fun countAll() {
        on(osmDb.countAll()).thenReturn(123L)
        on(notesDb.countAll()).thenReturn(245L)
        assertEquals(123L + 245L, ctrl.countAll())
    }

    @Test fun `hide osm quest`() {
        val quest = OsmQuestKey(NODE, 1, QUEST_TYPE.name)
        on(osmDb.getTimestamp(quest)).thenReturn(123)

        ctrl.hide(quest)

        verify(osmDb).add(quest)
        verify(listener).onHid(quest, 123)
    }

    @Test fun `hide osm note quest`() {
        val quest = OsmNoteQuestKey(2)
        on(notesDb.getTimestamp(quest.noteId)).thenReturn(123)

        ctrl.hide(quest)

        verify(notesDb).add(quest.noteId)
        verify(listener).onHid(quest, 123)
    }

    @Test fun `unhide osm quest`() {
        val quest = OsmQuestKey(NODE, 1, QUEST_TYPE.name)

        on(osmDb.getTimestamp(quest)).thenReturn(123)
        on(osmDb.delete(quest)).thenReturn(true).thenReturn(false)

        assertTrue(ctrl.unhide(quest))
        assertFalse(ctrl.unhide(quest))

        verify(osmDb, times(2)).delete(quest)
        verify(listener, times(1)).onUnhid(quest, 123)
    }

    @Test fun `unhide osm note quest`() {
        val quest = OsmNoteQuestKey(2)

        on(notesDb.getTimestamp(quest.noteId)).thenReturn(123)
        on(notesDb.delete(quest.noteId)).thenReturn(true).thenReturn(false)

        assertTrue(ctrl.unhide(quest))
        assertFalse(ctrl.unhide(quest))

        verify(notesDb, times(2)).delete(quest.noteId)
        verify(listener, times(1)).onUnhid(quest, 123)
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
