package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHiddenAt
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestHiddenAt
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.osmQuestKey
import dev.mokkery.answering.repeat
import dev.mokkery.answering.sequentially
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestsHiddenControllerImplTest {

    private lateinit var osmDb: OsmQuestsHiddenDao
    private lateinit var notesDb: NoteQuestsHiddenDao

    private lateinit var ctrl: QuestsHiddenController

    private lateinit var listener: QuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        osmDb = mock() {
            every { getAll() } returns listOf()
        }
        notesDb = mock() {
            every { getAll() } returns listOf()
        }
        listener = mock()
        ctrl = QuestsHiddenControllerImpl(osmDb, notesDb)
        ctrl.addListener(listener)
    }

    @Test fun get() {
        val q1 = osmQuestKey(elementId = 1)
        val q2 = osmQuestKey(elementId = 2)
        val q3 = OsmNoteQuestKey(3)
        val q4 = OsmNoteQuestKey(4)
        every { osmDb.getAll() } returns listOf(OsmQuestHiddenAt(q1, 123L))
        every { notesDb.getAll() } returns listOf(NoteQuestHiddenAt(q3.noteId, 124L))
        every { notesDb.getTimestamp(q4.noteId) } returns null

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

        every { osmDb.getAll() } returns listOf(h1, h2)
        every { notesDb.getAll() } returns listOf(h3, h4)

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

        every { osmDb.getAll() } returns listOf(h1)
        every { notesDb.getAll() } returns listOf(h2)
        assertEquals(2, ctrl.countAll())
    }

    @Test fun `hide osm quest`() {
        val q = osmQuestKey(elementId = 1)
        every { osmDb.getTimestamp(q) } returns 123L

        ctrl.hide(q)

        verify { osmDb.add(q) }
        verify { listener.onHid(q, 123) }
    }

    @Test fun `hide osm note quest`() {
        val q = OsmNoteQuestKey(1)
        every { notesDb.getTimestamp(q.noteId) } returns 123L

        ctrl.hide(q)

        verify { notesDb.add(q.noteId) }
        verify { listener.onHid(q, 123) }
    }

    @Test fun `unhide osm quest`() {
        val q = osmQuestKey()
        every { osmDb.delete(q) } sequentially {
            returns(true)
            repeat { returns(false) }
        }
        every { osmDb.getTimestamp(q) } sequentially {
            returns(123L)
            repeat { returns(null) }
        }

        assertTrue(ctrl.unhide(q))
        assertFalse(ctrl.unhide(q))

        verify(exactly(2)) { osmDb.getTimestamp(q) }
        verify(exactly(1)) { osmDb.delete(q) }
        verify(exactly(1)) { listener.onUnhid(q, 123) }
    }

    @Test fun `unhide osm note quest`() {
        val q = OsmNoteQuestKey(2)

        every { notesDb.delete(q.noteId) } sequentially {
            returns(true)
            repeat { returns(false) }
        }
        every { notesDb.getTimestamp(q.noteId) } sequentially {
            returns(123L)
            repeat { returns(null) }
        }

        assertTrue(ctrl.unhide(q))
        assertFalse(ctrl.unhide(q))

        verify(exactly(2)) { notesDb.getTimestamp(q.noteId) }
        verify(exactly(1)) { notesDb.delete(q.noteId) }
        verify(exactly(1)) { listener.onUnhid(q, 123) }
    }

    @Test fun unhideAll() {
        every { osmDb.deleteAll() } returns 7
        every { notesDb.deleteAll() } returns 9

        assertEquals(7 + 9, ctrl.unhideAll())

        verify { osmDb.deleteAll() }
        verify { notesDb.deleteAll() }
        verify { listener.onUnhidAll() }
    }
}
