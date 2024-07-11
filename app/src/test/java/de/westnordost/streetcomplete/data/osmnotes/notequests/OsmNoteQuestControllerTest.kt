package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.argThat
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.comment
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OsmNoteQuestControllerTest {

    private lateinit var noteSource: NotesWithEditsSource
    private lateinit var hiddenDB: NoteQuestsHiddenDao
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences

    private lateinit var ctrl: OsmNoteQuestController
    private lateinit var listener: OsmNoteQuestSource.Listener
    private lateinit var hideListener: OsmNoteQuestsHiddenSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginSource.Listener

    @BeforeTest fun setUp() {
        noteSource = mock()
        hiddenDB = mock()
        userDataSource = mock()
        userLoginSource = mock()
        prefs = mock()

        listener = mock()
        hideListener = mock()

        on(noteSource.addListener(any())).then { invocation ->
            noteUpdatesListener = invocation.getArgument(0)
            Unit
        }

        on(userLoginSource.addListener(any())).then { invocation ->
            userLoginListener = invocation.getArgument(0)
            Unit
        }

        ctrl = OsmNoteQuestController(noteSource, hiddenDB, userDataSource, userLoginSource, prefs)
        ctrl.addListener(listener)
        ctrl.addListener(hideListener)
    }

    @Test fun hide() {
        val note = note(1)
        val ts = 123L

        on(hiddenDB.getTimestamp(1)).thenReturn(ts)
        on(noteSource.get(1)).thenReturn(note)

        ctrl.hide(1)

        verify(hiddenDB).add(1)
        verify(hideListener).onHid(eq(OsmNoteQuestHidden(note, ts)))
        verify(listener).onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestIds = eq(listOf(1))
        )
    }

    @Test fun unhide() {
        val note = note(1)
        val ts = 123L

        on(hiddenDB.getTimestamp(1)).thenReturn(ts)
        on(noteSource.get(1)).thenReturn(note)
        on(hiddenDB.delete(1)).thenReturn(true)
        on(prefs.showAllNotes).thenReturn(true)

        ctrl.unhide(1)

        verify(hideListener).onUnhid(eq(OsmNoteQuestHidden(note, ts)))
        verify(listener).onUpdated(
            addedQuests = eq(listOf(OsmNoteQuest(1, note.position))),
            deletedQuestIds = eq(emptyList())
        )
    }

    @Test fun unhideAll() {
        val hiddenNoteIds = listOf<Long>(1, 2, 3)
        val hiddenNotes = listOf(
            note(1), note(2), note(3)
        )

        on(hiddenDB.getAllIds()).thenReturn(hiddenNoteIds)
        on(noteSource.getAll(hiddenNoteIds)).thenReturn(hiddenNotes)
        on(prefs.showAllNotes).thenReturn(true)

        ctrl.unhideAll()

        val expectedQuests = hiddenNotes.map { OsmNoteQuest(it.id, it.position) }

        verify(hiddenDB).deleteAll()
        verify(hideListener).onUnhidAll()
        verify(listener).onUpdated(
            addedQuests = eq(expectedQuests),
            deletedQuestIds = eq(emptyList())
        )
    }

    @Test fun getAllHiddenNewerThan() {
        val note1 = note(1)
        val note2 = note(2)

        on(hiddenDB.getNewerThan(123L)).thenReturn(listOf(
            NoteIdWithTimestamp(1, 300),
            NoteIdWithTimestamp(2, 500),
            NoteIdWithTimestamp(3, 600), // missing note
        ))
        on(noteSource.getAll(eq(listOf(1L, 2L, 3L)))).thenReturn(listOf(note1, note2))

        assertEquals(
            listOf(
                OsmNoteQuestHidden(note1, 300),
                OsmNoteQuestHidden(note2, 500),
            ),
            ctrl.getAllHiddenNewerThan(123L)
        )
    }

    @Test fun countAll() {
        on(hiddenDB.countAll()).thenReturn(123L)
        assertEquals(123L, ctrl.countAll())
    }

    @Test fun `get hidden returns null`() {
        on(noteSource.get(1)).thenReturn(note(1))
        on(hiddenDB.contains(1)).thenReturn(true)
        assertNull(ctrl.getVisible(1))
    }

    @Test fun `get missing returns null`() {
        on(noteSource.get(1)).thenReturn(null)
        assertNull(ctrl.getVisible(1))
    }

    @Test fun `get note quest with comment from user returns null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test?", user = User(id = 100, "Blaubär")),
            comment(text = "test", user = User(id = 1, "Blubbi"))
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNull(ctrl.getVisible(1))
    }

    @Test fun `get note quest with comment from user that contains a survey required marker returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test?", user = User(id = 100, "Blaubär")),
            comment(text = "ok but #surveyme", user = User(id = 1, "Blubbi")),
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNotNull(ctrl.getVisible(1))
    }

    @Test fun `get quest not phrased as question returns null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test")
        )))
        on(prefs.showAllNotes).thenReturn(false)

        assertNull(ctrl.getVisible(1))
    }

    @Test fun `get quest phrased as question returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test?"))
        ))
        on(prefs.showAllNotes).thenReturn(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    @Test fun `get quest phrased as question in other scripts returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(1, comments = listOf(comment(text = "Greek question mark: ;"))))
        on(noteSource.get(2)).thenReturn(note(2, comments = listOf(comment(text = "semicolon: ;"))))
        on(noteSource.get(3)).thenReturn(note(3, comments = listOf(comment(text = "mirrored question mark: ؟"))))
        on(noteSource.get(4)).thenReturn(note(4, comments = listOf(comment(text = "Armenian question mark: ՞"))))
        on(noteSource.get(5)).thenReturn(note(5, comments = listOf(comment(text = "Ethiopian question mark: ፧"))))
        on(noteSource.get(6)).thenReturn(note(6, comments = listOf(comment(text = "Vai question mark: ꘏"))))
        on(noteSource.get(7)).thenReturn(note(7, comments = listOf(comment(text = "full width question mark: ？"))))
        on(prefs.showAllNotes).thenReturn(false)

        assertEquals(1, ctrl.getVisible(1)?.id)
        assertEquals(2, ctrl.getVisible(2)?.id)
        assertEquals(3, ctrl.getVisible(3)?.id)
        assertEquals(4, ctrl.getVisible(4)?.id)
        assertEquals(5, ctrl.getVisible(5)?.id)
        assertEquals(6, ctrl.getVisible(6)?.id)
        assertEquals(7, ctrl.getVisible(7)?.id)
    }

    @Test fun `get quest with comment containing survey required marker returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test #surveyme"))
        ))
        on(prefs.showAllNotes).thenReturn(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    @Test fun `get quest not phrased as question returns non-null by preference`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test"))
        ))
        on(prefs.showAllNotes).thenReturn(true)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.getVisible(1))
    }

    // not doing all the tests for getAll again because it uses the same functions

    @Test fun getAll() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))

        on(hiddenDB.getAllIds()).thenReturn(emptyList())
        on(noteSource.getAll(bbox)).thenReturn(notes)
        on(prefs.showAllNotes).thenReturn(true)

        val expectedQuests = notes.map { OsmNoteQuest(it.id, it.position) }

        assertEquals(
            expectedQuests,
            ctrl.getAllVisibleInBBox(bbox)
        )
    }

    @Test fun `calls onInvalidated when logged in`() {
        userLoginListener.onLoggedIn()

        verify(listener).onInvalidated()
    }

    @Test fun `calls onInvalidated when cleared notes`() {
        noteUpdatesListener.onCleared()
        verify(listener).onInvalidated()
    }

    @Test fun `calls onUpdated when notes changed`() {
        // note 1 is added
        // note 2 is not eligible
        // note 3 is added/updated
        // note 4 is not eligible -> delete it
        // note 5 is deleted

        on(hiddenDB.getAllIds()).thenReturn(listOf(2, 4))
        on(prefs.showAllNotes).thenReturn(true)

        noteUpdatesListener.onUpdated(
            added = listOf(
                note(1),
                note(2)
            ),
            updated = listOf(note(3), note(4)),
            deleted = listOf(5)
        )

        verify(listener).onUpdated(
            addedQuests = argThat {
                it.containsExactlyInAnyOrder(listOf(
                    OsmNoteQuest(1, p()),
                    OsmNoteQuest(3, p()),
                ))
            },
            deletedQuestIds = argThat { it.containsExactlyInAnyOrder(listOf(4, 5)) }
        )
    }
}
