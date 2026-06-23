package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.noteEdit
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsyncedChangesCountSourceTest {
    private lateinit var noteEditsSource: NoteEditsSource
    private lateinit var elementEditsSource: ElementEditsSource

    private lateinit var noteEditsListener: NoteEditsSource.Listener
    private lateinit var elementEditsListener: ElementEditsSource.Listener

    private lateinit var listener: UnsyncedChangesCountSource.Listener

    private lateinit var source: UnsyncedChangesCountSource

    private val baseCount = 3 + 4

    @BeforeTest fun setUp() {
        noteEditsSource = mock() {
            every { addListener(any()) } calls { (listener: NoteEditsSource.Listener) ->
                noteEditsListener = listener
            }
        }

        elementEditsSource = mock() {
            every { addListener(any()) } calls { (listener: ElementEditsSource.Listener) ->
                elementEditsListener = listener
            }
        }

        every { noteEditsSource.getUnsyncedCount() } returns 3
        every { elementEditsSource.getUnsyncedCount() } returns 4
        every { elementEditsSource.getPositiveUnsyncedCount() } returns 2

        source = UnsyncedChangesCountSource(noteEditsSource, elementEditsSource)

        listener = mock()
        source.addListener(listener)
    }

    @Test fun count() = runBlocking {
        assertEquals(baseCount, source.getCount())
    }

    @Test fun `add unsynced element edit triggers listener`() {
        val edit = edit(isSynced = false)
        elementEditsListener.onAddedEdit(edit)
        verify { listener.onIncreased() }
    }

    @Test fun `remove unsynced element edit triggers listener`() {
        val edit = edit(isSynced = false)
        elementEditsListener.onDeletedEdits(listOf(edit))
        verify { listener.onDecreased() }
    }

    @Test fun `add synced element edit does not trigger listener`() {
        val edit = edit(isSynced = true)
        elementEditsListener.onAddedEdit(edit)
        verifyNoMoreCalls(listener)
    }

    @Test fun `remove synced element edit does not trigger listener`() {
        val edit = edit(isSynced = true)
        elementEditsListener.onDeletedEdits(listOf(edit))
        verifyNoMoreCalls(listener)
    }

    @Test fun `add note edit triggers listener`() {
        noteEditsListener.onAddedEdit(noteEdit())
        verify { listener.onIncreased() }
    }

    @Test fun `remove note edit triggers listener`() {
        noteEditsListener.onDeletedEdits(listOf(noteEdit()))
        verify { listener.onDecreased() }
    }

    @Test fun `marked note edit synced triggers listener`() {
        noteEditsListener.onSyncedEdit(noteEdit())
        verify { listener.onDecreased() }
    }
}
