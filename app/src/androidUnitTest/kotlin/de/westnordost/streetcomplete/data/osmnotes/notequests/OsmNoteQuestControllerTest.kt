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
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences

    private lateinit var ctrl: OsmNoteQuestController
    private lateinit var listener: OsmNoteQuestSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginSource.Listener

    @BeforeTest fun setUp() {
        noteSource = mock()
        userDataSource = mock()
        userLoginSource = mock()
        prefs = mock()

        listener = mock()

        on(noteSource.addListener(any())).then { invocation ->
            noteUpdatesListener = invocation.getArgument(0)
            Unit
        }

        on(userLoginSource.addListener(any())).then { invocation ->
            userLoginListener = invocation.getArgument(0)
            Unit
        }

        ctrl = OsmNoteQuestController(noteSource, userDataSource, userLoginSource, prefs)
        ctrl.addListener(listener)
    }

    @Test fun `get missing returns null`() {
        on(noteSource.get(1)).thenReturn(null)
        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest with comment from user returns null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test?", user = User(id = 100, "Blaubär")),
            comment(text = "test", user = User(id = 1, "Blubbi"))
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest with comment from user that contains a survey required marker returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test?", user = User(id = 100, "Blaubär")),
            comment(text = "ok but #surveyme", user = User(id = 1, "Blubbi")),
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and without survey required marker returns null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "this is a non-question test note\n\nCreated via StreetComplete 60.1", user = User(id = 100, "Blaubär")),
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and with survey required marker returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "this is a non-question test note #surveyme\n\nCreated via StreetComplete 60.1", user = User(id = 100, "Blaubär")),
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get note quest created in app without comments and with survey required marker (ignore case) returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "this is a non-question test note #SurVEyMe\n\nCreated via StreetComplete 60.1", user = User(id = 100, "Blaubär")),
        )))
        on(userDataSource.userId).thenReturn(1)

        assertNotNull(ctrl.get(1))
    }

    @Test fun `get quest not phrased as question returns null`() {
        on(noteSource.get(1)).thenReturn(note(comments = listOf(
            comment(text = "test")
        )))
        on(prefs.showAllNotes).thenReturn(false)

        assertNull(ctrl.get(1))
    }

    @Test fun `get quest phrased as question returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test?"))
        ))
        on(prefs.showAllNotes).thenReturn(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
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

        assertEquals(1, ctrl.get(1)?.id)
        assertEquals(2, ctrl.get(2)?.id)
        assertEquals(3, ctrl.get(3)?.id)
        assertEquals(4, ctrl.get(4)?.id)
        assertEquals(5, ctrl.get(5)?.id)
        assertEquals(6, ctrl.get(6)?.id)
        assertEquals(7, ctrl.get(7)?.id)
    }

    @Test fun `get quest with comment containing survey required marker returns non-null`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test #surveyme"))
        ))
        on(prefs.showAllNotes).thenReturn(false)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
    }

    @Test fun `get quest not phrased as question returns non-null by preference`() {
        on(noteSource.get(1)).thenReturn(note(
            1,
            position = p(1.0, 1.0),
            comments = listOf(comment(text = "test"))
        ))
        on(prefs.showAllNotes).thenReturn(true)

        assertEquals(OsmNoteQuest(1, p(1.0, 1.0)), ctrl.get(1))
    }

    // not doing all the tests for getAll again because it uses the same functions

    @Test fun getAll() {
        val bbox = bbox()
        val notes = listOf(note(1), note(2), note(3))

        on(noteSource.getAll(bbox)).thenReturn(notes)
        on(prefs.showAllNotes).thenReturn(true)

        val expectedQuests = notes.map { OsmNoteQuest(it.id, it.position) }

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
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
        on(prefs.showAllNotes).thenReturn(true)

        noteUpdatesListener.onUpdated(
            added = listOf(note(1)),
            updated = listOf(note(2)),
            deleted = listOf(3)
        )

        verify(listener).onUpdated(
            added = argThat {
                it.containsExactlyInAnyOrder(listOf(OsmNoteQuest(1, p()), OsmNoteQuest(2, p())))
            },
            deleted = argThat { it.containsExactlyInAnyOrder(listOf(3)) }
        )
    }
}
